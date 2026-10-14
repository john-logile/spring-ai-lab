package com.example.ailab.api;

import com.example.ailab.week1.day1.WelcomeBoard;
import com.example.ailab.week1.day2.NotificationService;
import com.example.ailab.week1.day4.AccountService;
import com.example.ailab.week1.day4.TxReport;
import com.example.labautoconfigure.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

/**
 * Read-only window onto the Week 1 beans. The transactions call is blocking
 * JDBC, so it runs on {@code boundedElastic}, off the Netty event loop.
 */
@RestController
@RequestMapping("/api/week1")
public class Week1Controller {

    private final WelcomeBoard welcomeBoard;
    private final NotificationService notificationService;
    private final GreetingService greetingService;
    private final AccountService accountService;

    public Week1Controller(WelcomeBoard welcomeBoard,
                           NotificationService notificationService,
                           GreetingService greetingService,
                           AccountService accountService) {
        this.welcomeBoard = welcomeBoard;
        this.notificationService = notificationService;
        this.greetingService = greetingService;
        this.accountService = accountService;
    }

    @GetMapping("/welcome")
    public List<String> welcome() {
        return welcomeBoard.lines();
    }

    @GetMapping("/notify")
    public NotificationService.NotificationReceipt notify(
            @RequestParam(defaultValue = "hello") String message) {
        return notificationService.notify(message);
    }

    @GetMapping("/greeting")
    public Map<String, String> greeting() {
        return Map.of(
                "greet", greetingService.greet(),
                "origin", greetingService.origin());
    }

    @GetMapping("/transactions")
    public Mono<Map<String, TxReport>> transactions() {
        return Mono.fromCallable(() -> Map.of(
                        "directCall", accountService.transferDirect(),
                        "proxyCall", accountService.transferThroughProxy()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
