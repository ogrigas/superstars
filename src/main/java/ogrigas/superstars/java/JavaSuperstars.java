package ogrigas.superstars.java;

import ogrigas.superstars.github.*;
import ogrigas.superstars.http.Authorization;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class JavaSuperstars {

    private final GithubSearch githubSearch;
    private final GithubRepos githubRepos;
    private final GithubUserStarred githubUserStarred;
    private final int searchLimit;

    public JavaSuperstars(
        GithubSearch githubSearch,
        GithubRepos githubRepos,
        GithubUserStarred githubUserStarred,
        int searchLimit) {

        this.githubSearch = githubSearch;
        this.githubRepos = githubRepos;
        this.githubUserStarred = githubUserStarred;
        this.searchLimit = searchLimit;
    }

    public List<JavaFramework> list(Authorization auth, Comparator<JavaFramework> sorting) {
        RepoSearchQuery query = RepoSearchQuery.builder()
            .term("framework")
            .language("Java")
            .limit(searchLimit)
            .build();

        return githubSearch.topActiveRepositories(query).stream()
            .map(repo -> JavaFramework.builder()
                .owner(repo.owner())
                .name(repo.name())
                .description(repo.description())
                .license(repo.license())
                .repositoryUrl(repo.url())
                .starCount(repo.starCount())
                .contributorCount(githubRepos.totalContributors(repo.key()))
                .starredByMe(auth.provided() ? githubUserStarred.containsRepo(auth, repo.key()) : null)
                .build())
            .sorted(sorting)
            .collect(toList());
    }

    public void star(Authorization auth, RepoKey repo) {
        githubUserStarred.addRepo(auth, repo);
    }
}
