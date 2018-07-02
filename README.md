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

## Testing

    ./gradlew test

## Building and running

Build and run directly from sources:

    ./gradlew run

Build and run a Docker image: 

    ./gradlew docker --info

    docker run --rm -p 8080:8080 ogrigas/superstars

### Configuration via environment variables

- `LOCAL_PORT` (default is 8080)
- `GITHUB_URL` (default is https://api.github.com)
- `SUPERSTAR_LIMIT` (default is 10)
- `HTTP_LOGGING`: "BASIC", "HEADERS", "BODY" or "NONE" (default is "BASIC")

## Usage

Fetch most popular Java frameworks:

    curl -i http://localhost:8080/java-superstars

Sort by number of contributors, in ascending order:

    curl -i "http://localhost:8080/java-superstars?sortBy=contributorCount&direction=ascending"

Provide GitHub Basic credentials to include `starredByMe` flag in response:

    curl -i -u "username:password" http://localhost:8080/java-superstars

Alternatively, you may authenticate with GitHub token:

    curl -i -H "Authorization: token {TOKEN}" http://localhost:8080/java-superstars

Star a repo:

    curl -i -u "username:password" -X PUT http://localhost:8080/java-superstars/{owner}/{repo}/star

Unstar a repo:

    curl -i -u "username:password" -X DELETE http://localhost:8080/java-superstars/{owner}/{repo}/star

## Developer guidelines

- Use [Karma-style git commit messages](http://karma-runner.github.io/2.0/dev/git-commit-msg.html)
- Use [Lombok](https://projectlombok.org) annotations in Java data-classes to reduce boilerplate
- Use Groovy and Spock for writing readable BDD-style tests

## Known issues

If you don't provide credentials, you will quickly hit GitHub rate limit of max 60 requests per hour.
The number of queries could be dramatically reduced by using GraphQL for joins, e.g. repos<->contributors.
Unfortunately, GitHub does not allow anonymous use of GraphQL API as of this writing.
