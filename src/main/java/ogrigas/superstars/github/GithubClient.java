package ogrigas.superstars.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URL;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class GithubClient {

    private final Retrofit retrofit;

    public GithubClient(OkHttpClient okHttp, URL baseUrl) {
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
            return call.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
