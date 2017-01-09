package com.example.web;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.dashboard.DashboardService;
import com.example.dashboard.ReactorPerson;
import com.example.dashboard.ReactorPersonNotFoundException;
import com.example.dashboard.ReactorPersonRepository;
import com.example.integration.gitter.GitterMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Brian Clozel
 */
@Controller
public class DashboardController {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final DashboardService dashboardService;

	private final ReactorPersonRepository repository;

	@Autowired
	public DashboardController(DashboardService dashboardService, ReactorPersonRepository repository) {
		this.dashboardService = dashboardService;
		this.repository = repository;
	}

	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/reactor/people")
	@ResponseBody
	public Flux<ReactorPerson> findReactorPeople() {
		log.info("Will search for reactor people");
		return this.repository.findAll();
	}

	@GetMapping("/reactor/people/{id}")
	@ResponseBody
	public Mono<ReactorPerson> findReactorPerson(@PathVariable String id) {
		log.info("Will find one reactor person with id [" + id + "]");
		return this.repository.findOne(id)
				.otherwiseIfEmpty(Mono.error(new ReactorPersonNotFoundException(id)));
	}

	@ExceptionHandler
	public ResponseEntity handleNotFoundException(ReactorPersonNotFoundException exc) {
		log.info("Exception [" + exc + "] occurred!");
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/issues")
	public String issues(Model model) {
		log.info("I'm in the issues!");
		model.addAttribute("issues", this.dashboardService.findReactorIssues());
		return "issues";
	}

	@GetMapping("/chat")
	public String chat() {
		return "chat";
	}

	@GetMapping(path = "/chatMessages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public Flux<GitterMessage> chatMessages(@RequestParam(required = false, defaultValue = "10") String limit) {
		log.info("Get chat messages with limit [" + limit + "]");
		return this.dashboardService.getLatestChatMessages(Integer.parseInt(limit));
	}

	@GetMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@ResponseBody
	public Flux<GitterMessage> streamChatMessages() {
		log.info("Stream chat messages");
		return this.dashboardService.streamChatMessages();
	}

}
