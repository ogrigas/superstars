package ogrigas.superstars.java;

import ogrigas.superstars.github.GithubSearch;
import ogrigas.superstars.github.RepoSearchQuery;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class JavaSuperstars {

    private final GithubSearch githubSearch;
    private final int searchLimit;

    public JavaSuperstars(GithubSearch githubSearch, int searchLimit) {
        this.githubSearch = githubSearch;
        this.searchLimit = searchLimit;
    }

    public List<JavaFramework> list() {
        RepoSearchQuery query = RepoSearchQuery.builder()
            .term("framework")
            .language("Java")
            .limit(searchLimit)
            .build();

        return githubSearch.topActiveRepositories(query).stream()
            .map(repo -> JavaFramework.builder()
                .name(repo.name())
                .description(repo.description())
                .license(repo.license())
                .repositoryUrl(repo.url())
                .starCount(repo.starCount())
                .build())
            .collect(toList());
    }
}
