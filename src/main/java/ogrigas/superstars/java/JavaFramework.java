package ogrigas.superstars.java;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.net.URL;

@Value
@Builder
@AllArgsConstructor
@Accessors(fluent = true)
public class JavaFramework {
    String name;
    String description;
    String license;
    URL repositoryUrl;
    int starCount;
    int contributorCount;
}
