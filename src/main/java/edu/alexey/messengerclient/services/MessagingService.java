package edu.alexey.messengerclient.services;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import edu.alexey.messengerclient.dto.IncomingMessageDto;
import edu.alexey.messengerclient.entities.Contact;
import edu.alexey.messengerclient.entities.Conversation;
import edu.alexey.messengerclient.entities.Message;
import edu.alexey.messengerclient.repositories.ContactRepository;
import edu.alexey.messengerclient.repositories.ConversationRepository;
import edu.alexey.messengerclient.repositories.MessageRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class MessagingService {

	static final int LOAD_LIMIT = 99;

	private final ContactRepository contactRepository;
	private final MessageRepository messageRepository;
	private final ConversationRepository conversationRepository;

	private BiConsumer<List<Conversation>, List<Message>> onUpdatedCallback;

	private UUID clientUuid;
	private WebClient webClient;
	private volatile UUID mostRecentMessageUuid;

	public MessagingService(
			ContactRepository contactRepository,
			MessageRepository messageRepository,
			ConversationRepository conversationRepository) {

		this.contactRepository = contactRepository;
		this.messageRepository = messageRepository;
		this.conversationRepository = conversationRepository;
		this.webClient = null;
	}

	public void setOnUpdatedCallback(BiConsumer<List<Conversation>, List<Message>> onUpdatedCallback) {
		this.onUpdatedCallback = onUpdatedCallback;
	}

	public void start(URI baseUri, UUID clientUuid, String username, String password) {

		stop();

		this.clientUuid = clientUuid;
		this.webClient = WebClient.builder()
				.baseUrl(baseUri.toString())
				.filter(basicAuthentication(username, password))
				.build();

		synchronized (this) {

			Message mostRecentMessage = messageRepository.getTopBy(Sort.by("sentAt", "messageId").descending());
			if (mostRecentMessage != null) {
				this.mostRecentMessageUuid = mostRecentMessage.getMessageUuid();
			}
		}

		// register client
		webClient.post().uri("/client/{client_uuid}", clientUuid)
				.retrieve()
				.bodyToMono(Void.class)
				//.onErrorResume(e -> Mono.empty())
				.block();

		checkUpdates();
	}

	public void stop() {

		this.webClient = null;
	}

	private void checkUpdates() {

		if (webClient == null) {
			return;
		}

		Integer status = webClient.get().uri("/client/{client_uuid}", clientUuid)
				.retrieve()
				.bodyToMono(Integer.class)
				.onErrorResume(e -> Mono.empty())
				.block();

		if (status == null || status == 0) {
			return;
		}

		getIncomingMessages();
	}

	@Transactional
	private void getIncomingMessages() {

		ArrayList<Conversation> newConversations = new ArrayList<Conversation>();
		List<Message> newMessages;

		synchronized (this) {

			RequestHeadersSpec<?> requestSpec;

			if (mostRecentMessageUuid == null) {
				requestSpec = webClient.get()
						.uri("/messages/last/{limit}/client/{client_uuid}",
								LOAD_LIMIT,
								clientUuid);
			} else {
				requestSpec = webClient.get()
						.uri("/messages/since/{since_message_uuid}/client/{client_uuid}",
								mostRecentMessageUuid,
								clientUuid);
			}

			List<IncomingMessageDto> incomingMessages = requestSpec
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<IncomingMessageDto>>() {
					})
					.block();

			newMessages = incomingMessages.stream().map(dto -> {

				Message message = new Message();
				message.setMessageUuid(dto.messageUuid());
				message.setSentAt(dto.sentAt());
				message.setContent(dto.content());
				UUID countersideUuid = dto.senderUuid();
				if (countersideUuid == null) {
					message.setOwn(true);
					countersideUuid = dto.addresseeUuid();
				} else {
					message.setOwn(false);
				}
				Conversation conversation = supplyConversation(
						countersideUuid,
						newConversations::add);
				message.setConversation(conversation);

				return message;
			}).toList();

			contactRepository.flush();
			conversationRepository.flush();
			messageRepository.saveAllAndFlush(newMessages);
		}

		if (onUpdatedCallback != null) {
			try {
				onUpdatedCallback.accept(newConversations, newMessages);
			} catch (Exception e) {
				log.error("Error on handling of onUpdatedCallback", e);
			}
		}
	}

	private Conversation supplyConversation(UUID countersideUuid, Consumer<Conversation> consumeIfNew) {

		Contact contact = contactRepository.findByUserUuid(countersideUuid);

		Conversation conversation;
		if (contact == null) {

			String contactDisplayName = webClient.get()
					.uri("/user/{user_uuid}", countersideUuid)
					.retrieve()
					.bodyToMono(String.class)
					.block();

			contact = new Contact();
			contact.setUserUuid(countersideUuid);
			contact.setDisplayName(contactDisplayName);
			contactRepository.save(contact);

			conversation = new Conversation();
			conversation.setContact(contact);
			conversationRepository.save(conversation);
			consumeIfNew.accept(conversation);

		} else {

			conversation = conversationRepository.findByContact(contact);
			if (conversation == null) {
				conversation = new Conversation();
				conversation.setContact(contact);
				conversationRepository.save(conversation);
				consumeIfNew.accept(conversation);
			}
		}

		return conversation;
	}

	//	public static void main(String[] args) {
	//
	//		UUID clientUuid = UUID.randomUUID();
	//
	//		RequestHeadersSpec<?> request = WebClient.builder().baseUrl("http://localhost:8080")
	//				.build()
	//				.get()
	//				.uri("/messages/last/{limit}/client/{client_uuid}",
	//						LOAD_LIMIT,
	//						clientUuid);
	//
	//		System.out.println(request);
	//		request.httpRequest(req -> System.out.println(req.getURI().toString()))
	//				.exchangeToMono(resp -> Mono.empty())
	//				.onErrorResume(t -> {
	//					t.printStackTrace();
	//					return Mono.empty();
	//				})
	//				.block();
	//
	//	}

}
