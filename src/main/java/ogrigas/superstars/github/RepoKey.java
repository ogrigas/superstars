package ogrigas.superstars.github;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Builder
@Accessors(fluent = true)
public class RepoKey {
    String owner;
    String name;
}
