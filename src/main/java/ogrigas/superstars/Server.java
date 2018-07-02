package ogrigas.superstars;

import ogrigas.superstars.github.Github;
import ogrigas.superstars.github.GithubRepos;
import ogrigas.superstars.github.GithubSearch;
import ogrigas.superstars.github.GithubUserStarredRepos;
import ogrigas.superstars.http.HttpError;
import ogrigas.superstars.java.JavaSuperstarRoutes;
import ogrigas.superstars.java.JavaSuperstars;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
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
            .httpLogging(Level.valueOf(env.getOrDefault("HTTP_LOGGING", "BASIC")))
            .build();
        new Server(config);
    }

    Server(Config config) {
        Github github = new Github(okHttp(config), config.githubUrl());
        JavaSuperstars javaSuperstars = new JavaSuperstars(
            new GithubSearch(github),
            new GithubRepos(github),
            new GithubUserStarredRepos(github),
            config.superstarLimit());

        service = Service.ignite().port(config.localPort());
        setupErrorHandling(service);
        new JavaSuperstarRoutes(javaSuperstars).addTo(service);
        service.awaitInitialization();
        log.info("Server listening on port " + config.localPort());
    }

    private void setupErrorHandling(Service service) {
        service.exception(HttpError.class, (ex, req, resp) -> ex.writeTo(resp));
        service.notFound((req, resp) -> new HttpError(404, "Not Found").writeTo(resp));
        service.internalServerError((req, resp) -> new HttpError(500, "Internal Error").writeTo(resp));
    }

    void stop() {
        service.stop();
        log.info("Server stopped");
    }

    private static OkHttpClient okHttp(Config config) {
        Logger httpLog = LoggerFactory.getLogger("http.client");
        return new OkHttpClient.Builder()
            .connectTimeout(5, SECONDS)
            .retryOnConnectionFailure(true)
            .readTimeout(20, SECONDS)
            .writeTimeout(20, SECONDS)
            .connectionPool(new ConnectionPool(1, 10, MINUTES))
            .addInterceptor(new HttpLoggingInterceptor(httpLog::debug).setLevel(config.httpLogging()))
            .build();
    }
}
