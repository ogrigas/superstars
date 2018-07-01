package ogrigas.superstars.github;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Builder
@Accessors(fluent = true)
public class RepoSearchQuery {
    String term;
    String language;
    int limit;
}
