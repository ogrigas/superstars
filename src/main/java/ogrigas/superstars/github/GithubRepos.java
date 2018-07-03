package ogrigas.superstars.github;

import ogrigas.superstars.http.Authorization;
import retrofit2.Response;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubRepos {

    private static final Pattern PAGE_PARAM_PATTERN = Pattern.compile(".*\\bpage=(\\d+).*");

    private final Github github;
    private final Api api;

    public GithubRepos(Github github) {
        this.github = github;
        this.api = github.proxy(Api.class);
    }

    /**
     * Retrieves total number of contributors to the given repository, including anonymous ones.
     * For efficiency, instead of fetching all contributors, we send a HEAD request with page size of 1
     * and then extract last page number from the "Link" header.
     */
    public CompletableFuture<Integer> totalContributors(Authorization auth, RepoKey repo) {
        return api.repositoryContributors(auth.optionalHeader(), repo.owner(), repo.name(), true, 1)
            .whenComplete(github::handleErrors)
            .thenApply(response -> Links.parse(response.headers().get("Link")))
            .thenApply(links -> links.rel("last")
                .map(URL::getQuery)
                .map(PAGE_PARAM_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(1))
                .map(Integer::parseInt)
                .orElse(0));
    }

    private interface Api {

        @HEAD("/repos/{owner}/{repo}/contributors")
        CompletableFuture<Response<Void>> repositoryContributors(
            @Header("Authorization") String authorization,
            @Path("owner") String owner,
            @Path("repo") String repoName,
            @Query("anon") boolean includeAnonymous,
            @Query("per_page") int pageSize);
    }
}
