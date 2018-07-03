package ogrigas.superstars.java;

import lombok.Value;
import ogrigas.superstars.github.*;
import ogrigas.superstars.http.Authorization;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    List<JavaFramework> list(Authorization auth, Comparator<JavaFramework> sorting) {
        RepoSearchQuery query = RepoSearchQuery.builder()
            .term("framework")
            .language("Java")
            .limit(searchLimit)
            .build();

        return githubSearch.topActiveRepositories(auth, query)
            .join()
            .stream()
            .map(repo -> getExtraData(auth, repo.key()).thenApply(extra -> javaFramework(repo, extra)))
            .collect(toList())
            .stream()
            .map(CompletableFuture::join)
            .sorted(sorting)
            .collect(toList());
    }

    private CompletableFuture<ExtraData> getExtraData(Authorization auth, RepoKey repoKey) {
        CompletableFuture<Integer> countContributors = githubRepos.totalContributors(auth, repoKey);
        return auth.provided() ?
            countContributors.thenCombine(githubUserStarredRepos.contains(auth, repoKey), ExtraData::authenticated) :
            countContributors.thenApply(ExtraData::anonymous);
    }

    private static JavaFramework javaFramework(RepoSearchItem repo, ExtraData extra) {
        return JavaFramework.builder()
            .owner(repo.owner())
            .name(repo.name())
            .description(repo.description())
            .license(repo.license())
            .repositoryUrl(repo.url())
            .starCount(repo.starCount())
            .contributorCount(extra.contributorCount)
            .starredByMe(extra.starredByMe)
            .build();
    }

    void star(Authorization auth, RepoKey repo) {
        githubUserStarredRepos.add(auth, repo).join();
    }

    void unstar(Authorization auth, RepoKey repo) {
        githubUserStarredRepos.remove(auth, repo).join();
    }

    @Value
    private static class ExtraData {
        int contributorCount;
        Boolean starredByMe;

        static ExtraData authenticated(int contributorCount, boolean starredByMe) {
            return new ExtraData(contributorCount, starredByMe);
        }

        static ExtraData anonymous(Integer contributorCount) {
            return new ExtraData(contributorCount, null);
        }
    }
}
