package ogrigas.superstars;

import ogrigas.superstars.github.GithubClient;
import ogrigas.superstars.github.GithubSearch;
import ogrigas.superstars.java.JavaSuperstarRoutes;
import ogrigas.superstars.java.JavaSuperstars;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Server {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Service service;

    public static void main(String[] args) throws MalformedURLException {
        Map<String, String> env = System.getenv();
        Config config = Config.builder()
            .localPort(parseInt(env.getOrDefault("LOCAL_PORT", "8080")))
            .githubUrl(new URL(env.getOrDefault("GITHUB_URL", "https://api.github.com")))
            .superstarLimit(parseInt(env.getOrDefault("SUPERSTAR_LIMIT", "10")))
            .build();
        new Server(config);
    }

    Server(Config config) {
        GithubClient githubClient = new GithubClient(okHttp(), config.githubUrl());
        JavaSuperstars javaSuperstars = new JavaSuperstars(new GithubSearch(githubClient), config.superstarLimit());

        service = Service.ignite().port(config.localPort());
        new JavaSuperstarRoutes(javaSuperstars).addTo(service);
        service.awaitInitialization();
        log.info("Server listening on port " + config.localPort());
    }

    void stop() {
        service.stop();
        log.info("Server stopped");
    }

    private static OkHttpClient okHttp() {
        return new OkHttpClient.Builder()
            .connectTimeout(5, SECONDS)
            .retryOnConnectionFailure(true)
            .readTimeout(20, SECONDS)
            .writeTimeout(20, SECONDS)
            .connectionPool(new ConnectionPool(1, 10, MINUTES))
            .build();
    }
}
