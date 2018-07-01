package ogrigas.superstars.github;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.net.URL;

@Value
@Builder
@Accessors(fluent = true)
public class RepoSearchItem {
    String owner;
    String name;
    String description;
    String license;
    URL url;
    int starCount;
}
