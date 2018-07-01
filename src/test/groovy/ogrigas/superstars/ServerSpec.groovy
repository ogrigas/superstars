package ogrigas.superstars

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit.WireMockRule
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.Rule
import spock.lang.Shared
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

class ServerSpec extends Specification {

    @Shared localPort = 60000
    @Shared githubPort = 60001

    @Rule WireMockRule github = new WireMockRule(githubPort)

    @Shared config = Config.builder()
        .localPort(localPort)
        .githubUrl(new URL("http://localhost:$githubPort"))
        .superstarLimit(3)
        .build()

    @Shared server = new Server(config)
    @Shared client = new OkHttpClient()

    void setup() {
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
    }

    void cleanupSpec() {
        server.stop()
    }

    def "fetches most popular Java frameworks"() {
        when:
        def response = request(path("/java-superstars"))

        then:
        github.verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
            .withQueryParam("q", equalTo("framework language:Java"))
            .withQueryParam("sort", equalTo("stars"))
            .withQueryParam("order", equalTo("desc"))
            .withQueryParam("per_page", equalTo(config.superstarLimit() as String)))

        response.code() == 200
        json(response) == [
            [
                name: "RepoA",
                description: "Description A",
                license: "License-A",
                repositoryUrl: "https://url.a",
                starCount: 333
            ],
            [
                name: "RepoB",
                description: "Description B",
                license: null,
                repositoryUrl: "https://url.b",
                starCount: 222
            ],
            [
                name: "RepoC",
                description: "Description C",
                license: null,
                repositoryUrl: "https://url.c",
                starCount: 111
            ]
        ]
    }

    private Request.Builder path(String path) {
        new Request.Builder().url("http://localhost:$localPort" + path)
    }

    private Response request(Request.Builder requestBuilder) {
        client.newCall(requestBuilder.build()).execute()
    }

    private static Object json(Response response) {
        assert response.body().contentType().toString() == 'application/json'
        return new ObjectMapper().readValue(response.body().string(), Object)
    }
}
