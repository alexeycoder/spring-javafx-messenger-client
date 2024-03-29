package edu.alexey.messengerclient.services;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.net.URI;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import edu.alexey.messengerclient.repositories.MessageRepository;
import reactor.core.publisher.Mono;

@Service
public class MessagingService {

	private final MessageRepository messageRepository;
	private UUID clientUuid;
	private WebClient webClient;

	public MessagingService(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
		this.webClient = WebClient.builder().baseUrl(null).build();
	}

	public void start(URI baseUri, UUID clientUuid, String username, String password) {
		stop();

		this.clientUuid = clientUuid;
		this.webClient = WebClient.builder()
				.baseUrl(baseUri.toString())
				.filter(basicAuthentication(username, password))
				.build();
	}

	public void stop() {

		// TODO: Need impl. 

	}

	private void checkUpdates() {

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

	private void getIncomingMessages() {

	}

}
