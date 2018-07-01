package ogrigas.superstars.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.ResponseTransformer;
import spark.Service;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;

public class JavaSuperstarRoutes {

    private final JavaSuperstars javaSuperstars;

    public JavaSuperstarRoutes(JavaSuperstars javaSuperstars) {
        this.javaSuperstars = javaSuperstars;
    }

    public void addTo(Service service) {
        ResponseTransformer toJson = new ObjectMapper().setVisibility(FIELD, ANY)::writeValueAsString;
        service.after((req, resp) -> resp.type("application/json"));

        service.get("/java-superstars", (req, resp) -> {
            return javaSuperstars.list();
        }, toJson);
    }
}
