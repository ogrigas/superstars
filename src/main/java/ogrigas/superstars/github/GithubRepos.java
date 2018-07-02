package ogrigas.superstars.github;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.HEAD;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubRepos {

    private static final Pattern PAGE_PARAM_PATTERN = Pattern.compile(".*\\bpage=(\\d+).*");

    private final GithubClient client;
    private final Api api;

    public GithubRepos(GithubClient client) {
        this.client = client;
        this.api = client.proxy(Api.class);
    }

    /**
     * Calculates total number of contributors to the given repository, including anonymous ones.
     * For efficiency, instead of fetching all contributors, we send a HEAD request for the first page
     * and then extract last page number from the "Link" header.
     */
    public int totalContributors(RepoKey repo) {
        Response response = client.request(api.repositoryContributors(repo.owner(), repo.name(), true, 1));
        Links links = Links.parse(response.headers().get("Link"));
        return links.rel("last")
            .map(URL::getQuery)
            .map(PAGE_PARAM_PATTERN::matcher)
            .filter(Matcher::matches)
            .map(m -> m.group(1))
            .map(Integer::parseInt)
            .orElse(0);
    }

    private interface Api {

        @HEAD("/repos/{owner}/{repo}/contributors")
        Call<Void> repositoryContributors(
            @Path("owner") String owner,
            @Path("repo") String repoName,
            @Query("anon") boolean includeAnonymous,
            @Query("per_page") int pageSize);
    }
}
