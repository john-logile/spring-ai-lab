# Week 1 review

The notification feature is the thread through all five days. `mvn test` includes `ApplicationTests.contextLoads()`.

```bash
mvn spring-boot:run
curl localhost:8080/api/week1/welcome
curl localhost:8080/api/week1/notify
curl localhost:8080/api/week1/greeting
curl localhost:8080/api/week1/transactions
curl localhost:8080/actuator/health
curl localhost:8080/actuator/info
```

Questions asked while reviewing this week:

1. [Day 3 — why `lab.greeting.enabled=true` does not use `AutoConfiguredGreetingService`](#question-why-labgreetingenabledtrue-does-not-use-autoconfiguredgreetingservice)
2. [Day 5 — `/actuator/metrics` only returns names](#question-actuator-metrics-only-returns-names)

## Day 1 — the container creates the objects

Spring's `ApplicationContext` is the IoC container. Code asks for a type; the container decides which instance to build and calls the constructor. That is dependency injection.

`WelcomeService` is registered three ways. `WelcomeBoard` receives all three through its constructor:

- `@Component` on `ComponentWelcomeService`. `@SpringBootApplication` scans `com.example.ailab` and registers every stereotype it finds.
- A `@Bean` method in `WelcomeBeanConfig`. Use this when you do not own the class. `BeanMethodWelcomeService` has no stereotype, so nothing creates it unless that method returns it.
- `ConfigurationWelcomeService` is a `@Configuration` class that implements the interface. `@Configuration` is itself a `@Component`, so the configuration class is a bean you can inject.

Constructor injection is the style this code uses. The dependency is `final`, it appears in the signature, and a test can call `new` with a fake. Field injection hides the dependency until something touches the field, and a test has to start the container or use reflection to set it.

`CircularLeft` and `CircularRight` each require the other in the constructor. They are `@Profile("circular")`, so a normal start does not create them. Turn them on with:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev,circular
```

Startup fails with `BeanCurrentlyInCreationException`: "Requested bean is currently in creation." Spring is still building `circularLeft` when `circularRight` asks for `circularLeft` again. Neither constructor can finish first. `CircularDependencyTest` asserts that failure, which is why `contextLoads()` still passes.

## Day 2 — same code, different settings

A bean is a singleton unless you say otherwise. `NotificationService` is one shared instance. `NotificationDraft` is `@Scope("prototype")`: each `drafts.getObject()` returns a new object with a new id. Injecting the draft directly into the singleton would freeze one instance at startup. Request and session scopes belong to servlet apps; this lab is WebFlux, so prototype is the scope you can see here.

`dev` and `prod` each register one `Notifier`. `LogNotifier` is `@Profile("dev")` and returns `log[dev@localhost]: ...`. `EmailNotifier` is `@Profile("prod")` and returns `email[alerts@example.com]: ...`. `spring.profiles.default=dev`, so a plain start logs. This switches the implementation with no code change:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod
```

`NotificationProperties` is a `@ConfigurationProperties(prefix = "notify")` record. `notify.api-key`, `notify.apiKey`, and `NOTIFY_API_KEY` all bind to `apiKey()`. `@Validated` and `@NotBlank` reject a blank key during startup, not on the first request. `Day2Config` enables the record with `@EnableConfigurationProperties`; a properties class is not a bean until something enables it. The configuration processor writes IDE metadata for those keys.

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=broken
```

That profile sets `notify.api-key=` and the app stops with `Property: notify.apiKey`, `Reason: must not be blank`.

## Day 3 — how Boot decides what to configure

A starter is a dependency list plus an auto-configuration. `@SpringBootApplication` includes `@EnableAutoConfiguration`, which reads `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Each class there is considered only after your own `@Configuration` classes exist, and only if its conditions match:

1. `@ConditionalOnClass` — the library is on the classpath.
2. `@ConditionalOnProperty` — a flag is on, or missing when `matchIfMissing = true`.
3. `@ConditionalOnMissingBean` — you did not already define that bean.

`GreetingAutoConfiguration` lives in `com.example.labautoconfigure`, outside the `com.example.ailab` scan, and is listed in that imports file. It creates a `GreetingService` only when `lab.greeting.enabled` is not false and no `GreetingService` bean exists. `GreetingOverrideConfig` defines one, so `/api/week1/greeting` says `origin: user bean`. `GreetingAutoConfigTest` loads the auto-config alone and gets `origin: auto-config`.

Open `org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration` from `spring-boot-starter-jdbc`. It is the same shape: no JDBC classes, no `DataSource`; you defined a `DataSource`, Boot backs off; otherwise it builds the H2 pool this lab uses.

### Question: why `lab.greeting.enabled=true` does not use `AutoConfiguredGreetingService`

`lab.greeting.enabled=true` does not select `AutoConfiguredGreetingService`. That flag only decides whether `GreetingAutoConfiguration` is allowed to run. The bean you actually get is decided by `@ConditionalOnMissingBean`.

Two conditions are checked, in order:

1. `@ConditionalOnProperty` on the class. `matchIfMissing = true` means the auto-config is already eligible when the property is absent. Setting it to `true` changes nothing. Setting it to `false` skips the whole auto-config.
2. `@ConditionalOnMissingBean(GreetingService.class)` on the `@Bean` method. `GreetingOverrideConfig` always defines a `GreetingService`, and user configuration runs before auto-configuration, so Boot sees that bean and skips `autoConfiguredGreetingService()`.

`/api/week1/greeting` therefore keeps returning `origin: user bean`.

To use `AutoConfiguredGreetingService` in the running app, remove the user bean and restart. Comment out the `@Bean` method in `GreetingOverrideConfig` (or the class). Leave `lab.greeting.enabled` unset, or set it to `true`. Then the greeting endpoint returns:

```text
Hello from auto-configuration
origin: auto-config
```

`GreetingAutoConfigTest.autoConfigSuppliesAGreetingWhenTheUserDoesNot` already does this in a context that never loads `GreetingOverrideConfig`. The next test adds that config back and shows the auto-config backing off.

If `lab.greeting.enabled=false` and the user bean is also removed, `Week1Controller` has nothing to inject and startup fails. If the property is `false` and `GreetingOverrideConfig` stays, the endpoint still returns `user bean`, because that `@Bean` does not read the property.

## Day 4 — annotations do not run unless the call hits the proxy

`@Transactional` is not compiled into the method. Spring subclasses `AccountService` with CGLIB (Boot's default, `spring.aop.proxy-target-class=true`) and the subclass starts and commits the transaction. A JDK dynamic proxy is the other option, used for interface beans only when that flag is false. Either proxy sees only calls that enter from outside.

`transferDirect()` calls `this.withdraw()`. That stays inside the object, so `REQUIRES_NEW` never runs. Both views show the transaction name `transferDirect` and the same JDBC connection. `transferThroughProxy()` calls `self.withdraw()`. `self` is the proxy, injected with `@Lazy` so this is not the Day 1 constructor cycle. `withdraw` then has its own transaction name and its own connection. `@Transactional`, `@Async`, and `@Cacheable` all work this way.

See it at `GET /api/week1/transactions`. Compare `directCall` and `proxyCall`.

## Day 5 — what an operator should see

`NotificationHealthIndicator` adds a `notification` component to `/actuator/health`. With the dev profile, details are visible and the component is `UP` with `mode: LOG`. The API key is not included. Prod sets `show-details=never` and exposes only `health` and `info`.

`LabInfoContributor` adds a `lab` section to `/actuator/info`, including the artifact and version from the Maven `build-info` goal. `env` is not in the exposure list, because that endpoint would print `notify.api-key`.

### Question: `/actuator/metrics` only returns names

`GET /actuator/metrics` is only the index. Each name is a separate endpoint, and that second call returns the numbers.

```bash
curl localhost:8080/actuator/metrics/jvm.memory.used
```

```json
{
  "name": "jvm.memory.used",
  "measurements": [
    { "statistic": "VALUE", "value": 123456789 }
  ],
  "availableTags": [
    { "tag": "area", "values": ["heap", "nonheap"] }
  ]
}
```

`measurements` is the value. `availableTags` are dimensions you can filter with `tag`:

```bash
curl 'localhost:8080/actuator/metrics/jvm.memory.used?tag=area:heap'
curl 'localhost:8080/actuator/metrics/http.server.requests?tag=uri:/api/week1/welcome&tag=method:GET'
```

Hit `/api/week1/welcome` once first. `http.server.requests` appears only after a request has been recorded.

This works on the default `dev` profile. The `prod` profile removes `metrics` from `management.endpoints.web.exposure.include`, so those URLs return 404 there.
