package com.example.ailab;

import com.example.ailab.week1.day1.WelcomeBoard;
import com.example.ailab.week1.day2.NotificationService;
import com.example.ailab.week1.day2.Notifier;
import com.example.ailab.week1.day4.AccountService;
import com.example.labautoconfigure.GreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("dev")
class ApplicationTests {

    @Autowired
    private WelcomeBoard welcomeBoard;

    @Autowired
    private Notifier notifier;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GreetingService greetingService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private WebTestClient client;

    @Test
    void contextLoads() {
        assertThat(welcomeBoard).isNotNull();
    }

    @Test
    void threeRegistrationStylesAreAllPresent() {
        assertThat(welcomeBoard.lines()).containsExactly(
                "@Bean — Welcome from a @Bean method",
                "@Component — Welcome from component scan",
                "@Configuration — Welcome from a @Configuration class");
    }

    @Test
    void devProfileLogsAndEachNotifyGetsANewDraft() {
        assertThat(notifier.deliver("hi")).isEqualTo("log[dev@localhost]: hi");
        var first = notificationService.notify("one");
        var second = notificationService.notify("two");
        assertThat(first.draftId()).isNotEqualTo(second.draftId());
    }

    @Test
    void userGreetingBeanBacksOffAutoConfig() {
        assertThat(greetingService.origin()).isEqualTo("user bean");
        assertThat(greetingService.greet()).isEqualTo("Hello from the application");
    }

    @Test
    void directCallJoinsTheOuterTransaction() {
        var report = accountService.transferDirect();
        assertThat(report.inner().transactionName()).isEqualTo(report.outer().transactionName());
        assertThat(report.inner().transactionName()).contains("transferDirect").doesNotContain("withdraw");
        assertThat(report.inner().connectionId()).isEqualTo(report.outer().connectionId());
    }

    @Test
    void proxyCallStartsANewTransaction() {
        var report = accountService.transferThroughProxy();
        assertThat(report.outer().transactionName()).contains("transferThroughProxy");
        assertThat(report.inner().transactionName()).contains("withdraw");
        assertThat(report.inner().newTransaction()).isTrue();
        assertThat(report.inner().connectionId()).isNotEqualTo(report.outer().connectionId());
    }

    @Test
    void healthAndInfoExposeTheWeek1Contributors() {
        client.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.components.notification.status").isEqualTo("UP")
                .jsonPath("$.components.notification.details.mode").isEqualTo("LOG");

        client.get().uri("/actuator/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.lab.week").isEqualTo(1)
                .jsonPath("$.lab.artifact").isEqualTo("spring-ai-lab");
    }
}
