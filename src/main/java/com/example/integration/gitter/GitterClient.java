package com.example.integration.gitter;

import com.example.DashboardProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Brian Clozel
 */
@Component
public class GitterClient {

	private final WebClient webClient;

	public GitterClient(DashboardProperties properties) {
		this.webClient = WebClient
				.builder(new ReactorClientHttpConnector())
				.filter(oAuthToken(properties.getGitter().getToken()))
				.build();
	}

	public Flux<GitterUser> getUsersInRoom(String roomId, int limit) {
		ClientRequest request = ClientRequest
				.GET("https://api.gitter.im/v1/rooms/{roomId}/users?limit={limit}", roomId, limit)
				.accept(MediaType.APPLICATION_JSON)
				.build();

		return this.webClient.exchange(request).flatMap(response -> response.bodyToFlux(GitterUser.class));
	}

	public Mono<GitterUser> findUserInRoom(String userName, String roomId) {
		ClientRequest request = ClientRequest
				.GET("https://api.gitter.im/v1/rooms/{roomId}/users?q={userName}", roomId, userName)
				.accept(MediaType.APPLICATION_JSON)
				.build();

		return this.webClient.exchange(request).then(response -> response.bodyToMono(GitterUser.class));
	}

	public Flux<GitterMessage> latestChatMessages(String roomId, int limit) {

		ClientRequest request = ClientRequest
				.GET("https://api.gitter.im/v1/rooms/{roomId}/chatMessages?limit={limit}", roomId, limit)
				.accept(MediaType.APPLICATION_JSON)
				.build();

		return this.webClient.exchange(request).flatMap(response -> response.bodyToFlux(GitterMessage.class));
	}

	public Flux<GitterMessage> streamChatMessages(String roomId) {

		ClientRequest request = ClientRequest
				.GET("https://stream.gitter.im/v1/rooms/{roomId}/chatMessages", roomId)
				.accept(MediaType.APPLICATION_JSON)
				.build();

		return this.webClient.exchange(request).flatMap(response -> response.bodyToFlux(GitterMessage.class));
	}

	private ExchangeFilterFunction oAuthToken(String token) {
		return (clientRequest, exchangeFunction) ->
				exchangeFunction
						.exchange(ClientRequest.from(clientRequest).header("Authorization", "Bearer " + token).build());
	}
}
