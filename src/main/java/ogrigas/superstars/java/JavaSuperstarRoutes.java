package ogrigas.superstars.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ogrigas.superstars.github.RepoKey;
import ogrigas.superstars.http.Authorization;
import spark.Request;
import spark.Response;
import spark.Service;

import java.util.Comparator;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;

public class JavaSuperstarRoutes {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
        .setVisibility(FIELD, ANY)
        .setDefaultPropertyInclusion(NON_NULL);

    private final JavaSuperstars javaSuperstars;

    public JavaSuperstarRoutes(JavaSuperstars javaSuperstars) {
        this.javaSuperstars = javaSuperstars;
    }

    public void addTo(Service service) {
        service.get("/java-superstars", (request, response) -> {
            Comparator<JavaFramework> sorting = JavaFramework.sorting(
                request.queryParamOrDefault("sortBy", ""),
                request.queryParamOrDefault("direction", "").equals("ascending")
            );
            return json(response, javaSuperstars.list(Authorization.from(request), sorting));
        });

        service.put("/java-superstars/:owner/:repoName/star", (request, response) -> {
            javaSuperstars.star(Authorization.from(request), repoKey(request));
            return noContent(response);
        });

        service.delete("/java-superstars/:owner/:repoName/star", (request, response) -> {
            javaSuperstars.unstar(Authorization.from(request), repoKey(request));
            return noContent(response);
        });
    }

    private static RepoKey repoKey(Request request) {
        return new RepoKey(request.params("owner"), request.params("repoName"));
    }

    private static String json(Response response, Object object) throws JsonProcessingException {
        String body = JSON_MAPPER.writeValueAsString(object);
        response.type("application/json");
        return body;
    }

    private static String noContent(Response response) {
        response.status(204);
        response.type("");
        response.header("Content-Length", "0");
        return "";
    }
}
