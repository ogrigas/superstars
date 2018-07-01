# Java Superstars

A JSON-over-HTTP API that retrieves 10 most active GitHub Java frameworks, in terms of the number of stargazers.
Stats include star count and total number of contributors.

Clients may optionally provide their GitHub credentials, which unlocks additional features:
- star a repo
- unstar a repo
- see whether they have starred a repo

## Setup

You will need:

- JDK 1.8+
- Docker
- To support Java annotation processing in your IDE:
  - IntelliJ IDEA: install "Lombok Plugin"
  - Eclipse: [read this](https://howtodoinjava.com/automation/lombok-eclipse-installation-examples/) 

## Running

    ./gradlew run

Supported environment variables:
- `LOCAL_PORT` (default 8080)
- `GITHUB_URL` (default https://api.github.com)
- `SUPERSTAR_LIMIT` (default 10)
- `HTTP_LOGGING`: BASIC, HEADERS, BODY or NONE (default BASIC)

### Guidelines

- Use [Karma-style git commit messages](http://karma-runner.github.io/2.0/dev/git-commit-msg.html)
- Use [Lombok](https://projectlombok.org) annotations in Java data-classes to reduce boilerplate
- Use Groovy and Spock for writing readable BDD-style tests
