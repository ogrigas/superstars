package ogrigas.superstars.github;

import ogrigas.superstars.http.Authorization;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class GithubUserStarred {

    private final GithubClient client;
    private final Api api;

    public GithubUserStarred(GithubClient client) {
        this.client = client;
        this.api = client.proxy(Api.class);
    }

    public boolean containsRepo(Authorization auth, RepoKey repo) {
        Call<Void> apiCall = api.get(auth.header(), repo.owner(), repo.name());
        return client.request(apiCall).code() == 204;
    }

    public void addRepo(Authorization auth, RepoKey repo) {
        client.request(api.put(auth.header(), repo.owner(), repo.name()));
    }

    private interface Api {

        @GET("/user/starred/{owner}/{repo}")
        Call<Void> get(
            @Header("Authorization") String authorization,
            @Path("owner") String owner,
            @Path("repo") String repoName);

        @PUT("/user/starred/{owner}/{repo}")
        Call<Void> put(
            @Header("Authorization") String authorization,
            @Path("owner") String owner,
            @Path("repo") String repoName);
    }
}
