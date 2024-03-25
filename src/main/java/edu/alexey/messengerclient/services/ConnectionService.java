package edu.alexey.messengerclient.services;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import edu.alexey.messengerclient.utils.CustomProperties;
import reactor.core.publisher.Mono;

@Service
public class ConnectionService {

	static final String URI_SCHEME = "http";

	private final CustomProperties customProperties;
	private final WebClient webClient;

	public ConnectionService(CustomProperties customProperties) {
		this.customProperties = customProperties;
		this.webClient = WebClient.builder().build();
	}

	public boolean checkConnection() {

		String host = customProperties.getServerHost();
		String port = customProperties.getServerPort();
		URI uri = UriComponentsBuilder.newInstance()
				.scheme(URI_SCHEME).host(host).port(port).path("hello")
				.build().toUri();

		Boolean isOk = webClient.get().uri(uri)
				.exchangeToMono(response -> Mono.just(response.statusCode().is2xxSuccessful()))
				.onErrorReturn(false)
				.block();
		return isOk;
	}

}
