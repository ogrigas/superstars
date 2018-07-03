package ogrigas.superstars.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import ogrigas.superstars.http.HttpError;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URL;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class Github {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Retrofit retrofit;

    public Github(OkHttpClient okHttp, URL baseUrl) {
        retrofit = new Retrofit.Builder()
            .client(okHttp)
            .baseUrl(baseUrl.toString())
            .addCallAdapterFactory(Java8CallAdapterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(
                new ObjectMapper().setVisibility(FIELD, ANY).disable(FAIL_ON_UNKNOWN_PROPERTIES)))
            .validateEagerly(true)
            .build();
    }

    <T> T proxy(Class<T> apiSpec) {
        return retrofit.create(apiSpec);
    }

    <R> Response<R> handleErrors(Response<R> response, Throwable ioError) {
        if (ioError != null) {
            log.error("I/O error when calling GitHub API", ioError);
            throw new HttpError(503, "GitHub API unavailable");
        }
        if (response.isSuccessful() || response.code() == 404) {
            return response;
        }
        if (response.code() == 401 || response.code() == 403) {
            throw new HttpError(response.code(), "Request rejected by GitHub");
        }
        log.error("GitHub returned HTTP {} error: {}", response.code(), errorBody(response));
        throw new HttpError(500, "GitHub API error " + response.code());
    }

    private static String errorBody(Response response) {
        try {
            return response.errorBody() != null ? response.errorBody().string() : "";
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
