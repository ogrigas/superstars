package ogrigas.superstars.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import ogrigas.superstars.github.RepoKey;
import ogrigas.superstars.http.Authorization;
import spark.ResponseTransformer;
import spark.Service;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;

public class JavaSuperstarRoutes {

    private final JavaSuperstars javaSuperstars;

    public JavaSuperstarRoutes(JavaSuperstars javaSuperstars) {
        this.javaSuperstars = javaSuperstars;
    }

    public void addTo(Service service) {
        ObjectMapper jsonMapper = new ObjectMapper().setVisibility(FIELD, ANY).setDefaultPropertyInclusion(NON_NULL);
        ResponseTransformer toJson = jsonMapper::writeValueAsString;
        service.after((req, resp) -> resp.type("application/json"));

        service.get("/java-superstars", (req, resp) ->
            javaSuperstars.list(
                Authorization.fromHeader(req.headers("Authorization")),
                JavaFramework.sorting(
                    req.queryParamOrDefault("sortBy", ""),
                    req.queryParamOrDefault("direction", "").equals("ascending"))),
            toJson);

        service.put("/java-superstars/:owner/:repoName/star", (req, resp) -> {
            javaSuperstars.star(
                Authorization.fromHeader(req.headers("Authorization")),
                new RepoKey(req.params("owner"), req.params("repoName")));
            resp.status(204);
            return "";
        });
    }
}
