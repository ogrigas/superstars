package ogrigas.superstars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class Server {

    private final Logger log = LoggerFactory.getLogger(getClass());

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
        log.info("Server listening on port " + config.localPort());
    }
}
