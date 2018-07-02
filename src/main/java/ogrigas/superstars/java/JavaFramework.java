package ogrigas.superstars.java;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import ogrigas.superstars.http.ClientError;

import java.net.URL;
import java.util.Comparator;

import static java.util.Comparator.comparingInt;

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
    Boolean starredByMe;

    static Comparator<JavaFramework> sorting(String field, boolean ascending) {
        Comparator<JavaFramework> order = fieldComparator(field.isEmpty() ? "starCount" : field);
        return ascending ? order : order.reversed();
    }

    private static Comparator<JavaFramework> fieldComparator(String field) {
        switch (field) {
            case "starCount":        return comparingInt(f -> f.starCount);
            case "contributorCount": return comparingInt(f -> f.contributorCount);
            default: throw new ClientError("invalid sort field name");
        }
    }
}
