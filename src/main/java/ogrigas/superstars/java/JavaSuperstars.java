package ogrigas.superstars.java;

import ogrigas.superstars.github.*;
import ogrigas.superstars.http.Authorization;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class JavaSuperstars {

    private final GithubSearch githubSearch;
    private final GithubRepos githubRepos;
    private final GithubUserStarredRepos githubUserStarredRepos;
    private final int searchLimit;

    public JavaSuperstars(
        GithubSearch githubSearch,
        GithubRepos githubRepos,
        GithubUserStarredRepos githubUserStarredRepos,
        int searchLimit) {

        this.githubSearch = githubSearch;
        this.githubRepos = githubRepos;
        this.githubUserStarredRepos = githubUserStarredRepos;
        this.searchLimit = searchLimit;
    }

    public List<JavaFramework> list(Authorization auth, Comparator<JavaFramework> sorting) {
        RepoSearchQuery query = RepoSearchQuery.builder()
            .term("framework")
            .language("Java")
            .limit(searchLimit)
            .build();

        return githubSearch.topActiveRepositories(auth, query).stream()
            .map(repo -> JavaFramework.builder()
                .owner(repo.owner())
                .name(repo.name())
                .description(repo.description())
                .license(repo.license())
                .repositoryUrl(repo.url())
                .starCount(repo.starCount())
                .contributorCount(githubRepos.totalContributors(auth, repo.key()))
                .starredByMe(auth.provided() ? githubUserStarredRepos.contains(auth, repo.key()) : null)
                .build())
            .sorted(sorting)
            .collect(toList());
    }

    public void star(Authorization auth, RepoKey repo) {
        githubUserStarredRepos.add(auth, repo);
    }

    public void unstar(Authorization auth, RepoKey repo) {
        githubUserStarredRepos.remove(auth, repo);
    }
}
