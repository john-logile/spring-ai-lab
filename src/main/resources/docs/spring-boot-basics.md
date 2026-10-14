# Spring Boot Basics (RAG demo doc)

Spring Boot makes it easy to create stand-alone, production-grade Spring
applications. Key ideas:

- **Auto-configuration**: starters like `spring-boot-starter-webflux` bring in
  a curated set of dependencies plus sensible defaults. You write business
  code; Boot wires the infrastructure.
- **Conventions**: `@SpringBootApplication` enables component scanning,
  auto-configuration, and property support in one annotation.
- **Properties**: `application.properties` (or YAML) externalizes configuration.
  Spring AI reads model names and base URLs from there.
- **Actuator**: `/actuator/health` and `/actuator/info` are on. Health includes
  a `notification` component. The `env` endpoint stays off so secrets are not dumped.

This document exists so the RAG module has something to retrieve.
Ask the RAG endpoint: "What is auto-configuration?"
