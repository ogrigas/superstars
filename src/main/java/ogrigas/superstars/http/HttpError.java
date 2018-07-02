package ogrigas.superstars.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Response;

import static java.util.Collections.singletonMap;

public class HttpError extends RuntimeException {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public final int statusCode;

    public HttpError(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public String writeTo(Response response) {
        response.status(statusCode);
        response.type("application/json");
        String json = toString();
        response.body(json);
        return json;
    }

    @Override
    public String toString() {
        try {
            return JSON_MAPPER.writeValueAsString(singletonMap("error", getMessage()));
        } catch (JsonProcessingException ignored) {
            return "{}";
        }
    }
}
