package ogrigas.superstars.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import ogrigas.superstars.http.HttpError;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
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
            .addConverterFactory(JacksonConverterFactory.create(
                new ObjectMapper().setVisibility(FIELD, ANY).disable(FAIL_ON_UNKNOWN_PROPERTIES)))
            .validateEagerly(true)
            .build();
    }

    public <T> T proxy(Class<T> apiSpec) {
        return retrofit.create(apiSpec);
    }

    public <R> Response<R> request(Call<R> call) {
        try {
            Response<R> response = call.execute();
            if (response.isSuccessful() || response.code() == 404) {
                return response;
            }
            if (response.code() == 401 || response.code() == 403) {
                throw new HttpError(response.code(), "Request rejected by GitHub");
            }
            ResponseBody body = response.errorBody();
            log.error("GitHub returned HTTP {} error: {}", response.code(), body != null ? body.string() : "");
            throw new HttpError(500, "GitHub API error " + response.code());
        } catch (IOException e) {
            log.error("I/O error calling GitHub API", e);
            throw new HttpError(503, "GitHub API unavailable");
        }
    }
}
