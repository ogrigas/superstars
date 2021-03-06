package ogrigas.superstars

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.BasicCredentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static okhttp3.logging.HttpLoggingInterceptor.Level

class ServerSpec extends Specification {

    @Shared localPort = 60000
    @Shared githubPort = 60001
    @Shared WireMockServer github = new WireMockServer(githubPort)

    @Shared config = Config.builder()
        .localPort(localPort)
        .githubUrl(new URL("http://localhost:$githubPort"))
        .superstarLimit(3)
        .httpLogging(Level.BASIC)
        .build()

    @Shared server = new Server(config)
    @Shared client = new OkHttpClient()

    void setupSpec() {
        github.start()
    }

    void setup() {
        github.resetAll()

        github.givenThat(get(urlPathEqualTo("/search/repositories")).willReturn(ok().withBody("""
            {
                "total_count": 99,
                "items": [
                    {
                        "unknown_field": "IGNORE",
                        "name": "RepoA",
                        "owner": { "login": "UserA" },
                        "license": { "name": "License-A" },
                        "description": "Description A",
                        "url": "https://url.a",
                        "stargazers_count": 333
                    },
                    {
                        "name": "RepoB",
                        "owner": { "login": "UserB" },
                        "description": "Description B",
                        "url": "https://url.b",
                        "stargazers_count": 222
                    },
                    {
                        "name": "RepoC",
                        "owner": { "login": "UserC" },
                        "description": "Description C",
                        "url": "https://url.c",
                        "stargazers_count": 111
                    }
                ]
            }
        """)))

        github.givenThat(head(urlPathEqualTo("/repos/UserA/RepoA/contributors")).willReturn(ok()
            .withHeader("Link", '<https://any.host/?page=11>; rel="last"')))

        github.givenThat(head(urlPathEqualTo("/repos/UserB/RepoB/contributors")).willReturn(ok()
            .withHeader("Link", '<https://any.host/?page=22>; rel="last"')))

        github.givenThat(head(urlPathEqualTo("/repos/UserC/RepoC/contributors")).willReturn(ok()))
    }

    void cleanupSpec() {
        server.stop()
        github.stop()
    }

    def "serves OpenAPI specification"() {
        when:
        def response = request(uri("/apidocs.yml"))

        then:
        response.code() == 200
        def doc = new ObjectMapper(new YAMLFactory()).readValue(response.body().string(), Map)
        doc.openapi == "3.0.0"
        doc.info?.title == "Java Superstars"
    }

    def "fetches most popular Java frameworks"() {
        when:
        def response = request(uri("/java-superstars"))

        then:
        github.verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
            .withoutHeader("Authorization")
            .withQueryParam("q", equalTo("framework language:Java"))
            .withQueryParam("sort", equalTo("stars"))
            .withQueryParam("order", equalTo("desc"))
            .withQueryParam("per_page", equalTo(config.superstarLimit() as String)))

        github.verify(3, headRequestedFor(urlPathMatching("/repos/.+/.+/contributors"))
            .withoutHeader("Authorization")
            .withQueryParam("anon", equalTo("true"))
            .withQueryParam("per_page", equalTo("1")))

        github.verify(0, anyRequestedFor(urlPathMatching("/user/starred/.*")))

        response.code() == 200
        json(response) == [
            [
                owner: "UserA",
                name: "RepoA",
                description: "Description A",
                license: "License-A",
                repositoryUrl: "https://url.a",
                starCount: 333,
                contributorCount: 11
            ],
            [
                owner: "UserB",
                name: "RepoB",
                description: "Description B",
                repositoryUrl: "https://url.b",
                starCount: 222,
                contributorCount: 22
            ],
            [
                owner: "UserC",
                name: "RepoC",
                description: "Description C",
                repositoryUrl: "https://url.c",
                starCount: 111,
                contributorCount: 0
            ]
        ]
    }

    @Unroll
    def "sorts results by '#field' in '#direction' order"() {
        when:
        def response = request(uri("/java-superstars?sortBy=${field}&direction=${direction}"))

        then:
        response.code() == 200
        json(response)*.name == resultingNames

        where:
        field              | direction    || resultingNames
        "starCount"        | "ascending"  || ["RepoC", "RepoB", "RepoA"]
        "starCount"        | "descending" || ["RepoA", "RepoB", "RepoC"]
        "starCount"        | ""           || ["RepoA", "RepoB", "RepoC"]
        "contributorCount" | "ascending"  || ["RepoC", "RepoA", "RepoB"]
        "contributorCount" | "descending" || ["RepoB", "RepoA", "RepoC"]
        "contributorCount" | ""           || ["RepoB", "RepoA", "RepoC"]
        ""                 | ""           || ["RepoA", "RepoB", "RepoC"]
    }

    def "rejects request to sort by unsupported field"() {
        when:
        def response = request(uri("/java-superstars?sortBy=invalidField"))

        then:
        response.code() == 400
        json(response) == [error: "Invalid sort field name"]
    }

    def "accepts GitHub credentials and returns which repositories were starred by the client"() {
        given:
        github.givenThat(get(urlPathEqualTo("/user/starred/UserA/RepoA")).willReturn(status(204)))
        github.givenThat(get(urlPathEqualTo("/user/starred/UserB/RepoB")).willReturn(status(404)))
        github.givenThat(get(urlPathEqualTo("/user/starred/UserC/RepoC")).willReturn(status(404)))

        when:
        def response = request(uri("/java-superstars").header("Authorization", basicAuth("USERNAME", "PASSWORD")))

        then:
        github.findAll(anyRequestedFor(anyUrl())).each {
            assert it.header("Authorization").firstValue() == basicAuth("USERNAME", "PASSWORD")
        }

        response.code() == 200
        json(response).collect { it.subMap(["name", "starredByMe"]) } == [
            [name: "RepoA", starredByMe: true],
            [name: "RepoB", starredByMe: false],
            [name: "RepoC", starredByMe: false]
        ]
    }

    def "supports token-based authentication"() {
        given:
        github.givenThat(get(urlPathMatching("/user/starred/.+/.+")).willReturn(status(204)))

        when:
        def response = request(uri("/java-superstars").header("Authorization", "token XYZ"))

        then:
        github.findAll(anyRequestedFor(anyUrl())).each {
            assert it.header("Authorization").firstValue() == "token XYZ"
        }

        response.code() == 200
    }

    def "stars a repository"() {
        given:
        github.givenThat(put(urlPathMatching("/user/starred/.*")).willReturn(status(204)))

        when:
        def response = request(uri("/java-superstars/UserA/RepoA/star")
            .put(emptyBody())
            .header("Authorization", basicAuth("USERNAME", "PASSWORD")))

        then:
        github.verify(putRequestedFor(urlPathEqualTo("/user/starred/UserA/RepoA"))
            .withHeader("Content-Length", equalTo("0"))
            .withBasicAuth(new BasicCredentials("USERNAME", "PASSWORD")))

        response.code() == 204
        response.header("Content-Type") == null
    }

    def "unstars a repository"() {
        given:
        github.givenThat(delete(urlPathMatching("/user/starred/.*")).willReturn(status(204)))

        when:
        def response = request(uri("/java-superstars/UserA/RepoA/star")
            .delete()
            .header("Authorization", basicAuth("USERNAME", "PASSWORD")))

        then:
        github.verify(deleteRequestedFor(urlPathEqualTo("/user/starred/UserA/RepoA"))
            .withBasicAuth(new BasicCredentials("USERNAME", "PASSWORD")))

        response.code() == 204
        response.header("Content-Type") == null
    }

    @Unroll
    def "requires credentials to #performAction"() {
        when:
        def response = request(unauthorizedRequest)

        then:
        github.verify(0, anyRequestedFor(anyUrl()))

        response.code() == 401
        json(response) == [error: "Authorization header required"]

        where:
        performAction   | unauthorizedRequest
        "star a repo"   | uri("/java-superstars/UserA/RepoA/star").put(emptyBody())
        "unstar a repo" | uri("/java-superstars/UserA/RepoA/star").delete()
    }

    @Unroll
    def "propagates GitHub error #githubStatus when #performingAction"() {
        given:
        github.givenThat(any(anyUrl()).willReturn(status(statusCode)))

        when:
        def response = request(incomingRequest.header("Authorization", "Basic any:credentials"))

        then:
        response.code() == statusCode
        json(response) == [error: "Request rejected by GitHub"]

        where:
        performingAction  | incomingRequest                                           | statusCode
        "listing repos"   | uri("/java-superstars").get()                             | 401
        "listing repos"   | uri("/java-superstars").get()                             | 403
        "starring repo"   | uri("/java-superstars/UserA/RepoA/star").put(emptyBody()) | 401
        "starring repo"   | uri("/java-superstars/UserA/RepoA/star").put(emptyBody()) | 403
        "unstarring repo" | uri("/java-superstars/UserA/RepoA/star").delete()         | 401
        "unstarring repo" | uri("/java-superstars/UserA/RepoA/star").delete()         | 403
    }

    def "handles GitHub server errors"() {
        given:
        github.givenThat(any(anyUrl()).willReturn(status(503)))

        when:
        def response = request(uri("/java-superstars"))

        then:
        response.code() == 500
        json(response) == [error: "GitHub API error 503"]
    }

    private Request.Builder uri(String uri) {
        new Request.Builder().url("http://localhost:$localPort" + uri)
    }

    private static String basicAuth(String username, String password) {
        "Basic " + "$username:$password".bytes.encodeBase64()
    }

    private static RequestBody emptyBody() {
        RequestBody.create(null, "")
    }

    private Response request(Request.Builder requestBuilder) {
        client.newCall(requestBuilder.build()).execute()
    }

    private static Object json(Response response) {
        assert response.header("Content-Type") == 'application/json'
        return new ObjectMapper().readValue(response.body().string(), Object)
    }
}
