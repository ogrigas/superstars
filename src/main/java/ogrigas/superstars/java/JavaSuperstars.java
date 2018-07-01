package ogrigas.superstars.java;

import ogrigas.superstars.github.GithubRepos;
import ogrigas.superstars.github.GithubSearch;
import ogrigas.superstars.github.RepoSearchQuery;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class JavaSuperstars {

    private final GithubSearch githubSearch;
    private final GithubRepos githubRepos;
    private final int searchLimit;

    public JavaSuperstars(GithubSearch githubSearch, GithubRepos githubRepos, int searchLimit) {
        this.githubSearch = githubSearch;
        this.githubRepos = githubRepos;
        this.searchLimit = searchLimit;
    }

    public List<JavaFramework> list(Comparator<JavaFramework> sorting) {
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
                .contributorCount(githubRepos.totalContributors(repo.owner(), repo.name()))
                .build())
            .sorted(sorting)
            .collect(toList());
    }
}
