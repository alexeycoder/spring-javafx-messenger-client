package edu.alexey.messengerclient.services;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import edu.alexey.messengerclient.dto.ContactDto;
import edu.alexey.messengerclient.dto.IncomingMessageDto;
import edu.alexey.messengerclient.dto.OutgoingMessageDto;
import edu.alexey.messengerclient.entities.Contact;
import edu.alexey.messengerclient.entities.Conversation;
import edu.alexey.messengerclient.entities.Message;
import edu.alexey.messengerclient.repositories.ContactRepository;
import edu.alexey.messengerclient.repositories.ConversationRepository;
import edu.alexey.messengerclient.repositories.MessageRepository;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class MessagingService {

	static final long REFRESH_PERIOD_SEC = 2;
	static final int LOAD_LIMIT = 99;
	static final String HEADER_KEY_CLIENT_ID = "EduAlexeyMessenger-Client-Id";

	private final ContactRepository contactRepository;
	private final MessageRepository messageRepository;
	private final ConversationRepository conversationRepository;

	private BiConsumer<List<Conversation>, List<Message>> onUpdatedCallback;

	private UUID clientUuid;
	private WebClient webClient;
	private UUID userUuid;
	private volatile UUID mostRecentMessageUuid;

	private ScheduledExecutorService scheduledExecutor;
	private volatile boolean isUpdating;

	public MessagingService(
			ContactRepository contactRepository,
			MessageRepository messageRepository,
			ConversationRepository conversationRepository) {

		this.contactRepository = contactRepository;
		this.messageRepository = messageRepository;
		this.conversationRepository = conversationRepository;
		this.webClient = null;
	}

	@PreDestroy
	void close() {
		stop();
	}

	public void setOnIncomingMessagesCallback(BiConsumer<List<Conversation>, List<Message>> onUpdatedCallback) {
		this.onUpdatedCallback = onUpdatedCallback;
	}

	public void start(URI baseUri, UUID clientUuid, String username, String password, UUID userUuid) {

		stop();

		this.userUuid = userUuid;
		this.clientUuid = clientUuid;
		webClient = WebClient.builder()
				.baseUrl(baseUri.toString())
				.filter(basicAuthentication(username, password))
				.defaultHeader(HEADER_KEY_CLIENT_ID, clientUuid.toString())
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

		// checkUpdates();
		scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.scheduleAtFixedRate(this::checkUpdates, 0, REFRESH_PERIOD_SEC, TimeUnit.SECONDS);
		log.info("Started scheduledExecutor");
	}

	public void stop() {
		if (scheduledExecutor != null) {
			log.info("Closing scheduledExecutor");
			scheduledExecutor.close();
			scheduledExecutor = null;
			isUpdating = false;
			log.info("Closed scheduledExecutor");
		}
		webClient = null;
	}

	private void checkUpdates() {

		log.info("Check updates");

		if (isUpdating) {
			return;
		}
		isUpdating = true;

		if (webClient == null) {
			isUpdating = false;
			return;
		}
		try {
			Integer status = webClient.get().uri("/client/{client_uuid}", clientUuid)
					.retrieve()
					.bodyToMono(Integer.class)
					.onErrorResume(e -> Mono.empty())
					.block();

			if (status == null || status == 0) {
				isUpdating = false;
				return;
			}

			getIncomingMessages();
		} catch (Exception e) {
			log.error("Exception has been thrown when checking updates", e);
		} finally {
			isUpdating = false;
		}
	}

	@Transactional
	private void getIncomingMessages() {

		ArrayList<Conversation> newConversations = new ArrayList<Conversation>();
		List<Message> newMessages;

		synchronized (this) {

			RequestHeadersSpec<?> requestSpec;

			if (mostRecentMessageUuid == null) {
				requestSpec = webClient.get()
						.uri(uriBuilder -> uriBuilder
								.path("/messages")
								.queryParam("limit", LOAD_LIMIT)
								.build());

			} else {
				requestSpec = webClient.get()
						.uri(uriBuilder -> uriBuilder
								.path("/messages")
								.queryParam("since_message", mostRecentMessageUuid.toString())
								.build());
			}

			List<IncomingMessageDto> incomingMessages = requestSpec
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<IncomingMessageDto>>() {
					})
					.block();

			if (incomingMessages.isEmpty()) {
				return;
			}

			newMessages = incomingMessages.stream().map(dto -> {

				Message message = new Message();
				message.setMessageUuid(dto.messageUuid());
				message.setSentAt(dto.sentAt());
				message.setContent(dto.content());
				UUID countersideUuid = dto.senderUuid();
				if (countersideUuid == null) {
					message.setOwn(true);
					countersideUuid = dto.addresseeUuid();
					if (countersideUuid == null) {
						countersideUuid = userUuid;
					}
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
			mostRecentMessageUuid = newMessages.getLast().getMessageUuid();
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
					.uri("/users/{user_uuid}", countersideUuid)
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

	public boolean sendMessage(int conversationId, String content) {

		Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
		if (conversationOpt.isEmpty()) {
			throw new IllegalStateException("Nonexistent conversation");
		}
		UUID addresseeUuid = conversationOpt.get().getContact().getUserUuid();

		ResponseEntity<Void> responseEntity = webClient.post().uri("/messages/new")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(new OutgoingMessageDto(addresseeUuid, content))
				.retrieve()
				.toEntity(Void.class)
				.block();

		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			return true;
		}
		return false;
	}

	public List<ContactDto> findUsersByUserUuid(String uuidPattern) {

		List<ContactDto> result = webClient.get().uri(uriBuilder -> uriBuilder
				.path("/users")
				.queryParam("uuid", uuidPattern.toLowerCase())
				.build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<ContactDto>>() {
				}).block();

		return result;
	}

	public List<ContactDto> findUsersByDisplayName(String displayNamePattern) {

		List<ContactDto> result = webClient.get().uri(uriBuilder -> uriBuilder
				.path("/users")
				.queryParam("display_name", displayNamePattern)
				.build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<ContactDto>>() {
				}).block();

		return result;
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
