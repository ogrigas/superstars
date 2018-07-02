package ogrigas.superstars.github;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class RepoKey {
    String owner;
    String name;
}
