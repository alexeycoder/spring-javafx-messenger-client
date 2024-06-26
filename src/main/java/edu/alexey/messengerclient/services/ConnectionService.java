package edu.alexey.messengerclient.services;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import edu.alexey.messengerclient.dto.SignupDto;
import edu.alexey.messengerclient.utils.CustomProperties;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ConnectionService {

	static final String URI_SCHEME = "http";

	private final CustomProperties customProperties;
	private final MessagingService messagingService;

	private final WebClient webClient;
	private URI baseUri;
	private String username;
	private String password;

	private UUID userUuid;

	public ConnectionService(CustomProperties customProperties, MessagingService messagingService) {
		this.customProperties = customProperties;
		this.messagingService = messagingService;
		this.webClient = WebClient.builder().build();
		actualizeFromCustomProperties();
		this.customProperties.addPropertyChangeListener(evt -> actualizeFromCustomProperties());
	}

	private void actualizeFromCustomProperties() {
		String host = customProperties.getServerHost();
		if (host == null || host.isBlank()) {
			this.baseUri = null;
		}

		String port = customProperties.getServerPort();

		this.baseUri = UriComponentsBuilder.newInstance()
				.scheme(URI_SCHEME).host(host).port(port)
				.build().toUri();

		this.username = customProperties.getUsername();
		this.password = customProperties.getPassword();
	}

	public boolean checkConnection() {
		if (baseUri == null) {
			return false;
		}

		URI uri = UriComponentsBuilder.fromUri(baseUri).path("/hello").build().toUri();
		Boolean isOk = webClient.get().uri(uri)
				.exchangeToMono(response -> Mono.just(response.statusCode().is2xxSuccessful()))
				.onErrorReturn(false)
				.block();
		return isOk;
	}

	public boolean trySignup(SignupDto signupData) {
		if (baseUri == null) {
			return false;
		}

		URI uri = UriComponentsBuilder.fromUri(baseUri).path("/users/signup").build().toUri();
		URI createdResourceLocation = webClient.post().uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(signupData)
				.exchangeToMono(response -> response.statusCode().is2xxSuccessful()
						? Mono.justOrEmpty(response.headers().asHttpHeaders().getLocation())
						: Mono.empty())
				.onErrorResume(e -> Mono.empty())
				.block();

		if (createdResourceLocation == null) {
			return false;
		}

		System.out.println("Created Resource Location is " + createdResourceLocation.getPath());

		// доп. проверка по совпадению displayName, указанного при регистрации, и
		// возвращаемого get методом возвращённой endpoint (локации) ресурса,
		// доступ к которому ещё не требует аутентификации:
		String responseStr = webClient.get().uri(
				UriComponentsBuilder.fromUri(baseUri).path(createdResourceLocation.getPath()).build().toUri())
				.retrieve().bodyToMono(String.class)
				.onErrorResume(e -> Mono.empty())
				.block();

		System.out.printf("Response string = %s%n", responseStr);
		if (!Objects.equals(responseStr, signupData.getDisplayName())) {
			return false;
		}

		// окончательная проверка авторизацией:
		return checkAuthorization(signupData.getUsername(), signupData.getPassword());
	}

	public boolean checkAuthorization() {

		return checkAuthorization(this.username, this.password);
	}

	private boolean checkAuthorization(String username, String password) {
		if (baseUri == null) {
			return false;
		}

		log.info("Checking authorization for {}", username);

		URI uri = UriComponentsBuilder.fromUri(baseUri).path("/client").build().toUri();

		userUuid = webClient.get().uri(uri)
				.headers(headers -> {
					headers.setBasicAuth(username, password);
				})
				.retrieve()
				.bodyToMono(UUID.class)
				.onErrorResume(t -> Mono.empty())
				.block();

		customProperties.setUserUuid(userUuid);
		log.info("Obtained User UUID {}", userUuid);

		return userUuid != null;
	}

	public boolean login() {
		log.info("Login initialed");
		if (checkAuthorization()) {

			log.info("Starting messaging service");
			messagingService.start(baseUri, customProperties.getClientUuid(), username, password, userUuid);
			log.info("Started messaging service");
			return true;
		}
		log.info("Login failed due to authorization fail");
		return false;
	}

}
