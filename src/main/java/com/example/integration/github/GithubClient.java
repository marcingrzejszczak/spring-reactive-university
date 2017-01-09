package com.example.integration.github;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.DashboardProperties;

import reactor.core.publisher.Flux;

/**
 * @author Brian Clozel
 */
@Component
public class GithubClient {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final MediaType VND_GITHUB_V3 = MediaType.valueOf("application/vnd.github.v3+json");

	private final WebClient webClient;

	public GithubClient(DashboardProperties properties) {
		this.webClient = WebClient
				.builder(new ReactorClientHttpConnector())
				.filter(ExchangeFilterFunctions
						.basicAuthentication(properties.getGithub().getUsername(),
								properties.getGithub().getToken()))
				.filter(userAgent())
				.build();
	}

	public Flux<GithubIssue> findOpenIssues(String owner, String repo) {
		log.info("Looking for open issues for owner [" + owner + "] and repo [" + repo + "]");
		ClientRequest request = ClientRequest
				.GET("https://api.github.com/repos/{owner}/{repo}/issues?state=open", owner, repo)
				.accept(VND_GITHUB_V3)
				.build();

		return this.webClient.exchange(request).flatMap(response -> response.bodyToFlux(GithubIssue.class));
	}


	private ExchangeFilterFunction userAgent() {
		return (clientRequest, exchangeFunction) -> {
			ClientRequest newRequest = ClientRequest
					.from(clientRequest)
					.header("User-Agent", "Spring Framework WebClient")
					.build();
			return exchangeFunction.exchange(newRequest);
		};
	}

}
