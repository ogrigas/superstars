package ogrigas.superstars;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import java.net.URL;

@Value
@Builder
@Accessors(fluent = true)
class Config {
    int localPort;
    URL githubUrl;
    int superstarLimit;
    Level httpLogging;
}
