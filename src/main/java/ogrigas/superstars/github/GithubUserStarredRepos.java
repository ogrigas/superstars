package ogrigas.superstars.github;

import ogrigas.superstars.http.Authorization;
import retrofit2.Call;
import retrofit2.http.*;

public class GithubUserStarredRepos {

    private final Github github;
    private final Api api;

    public GithubUserStarredRepos(Github github) {
        this.github = github;
        this.api = github.proxy(Api.class);
    }

    public boolean contains(Authorization auth, RepoKey repo) {
        return github.request(api.get(auth.requireHeader(), repo.owner(), repo.name())).code() == 204;
    }

    public void add(Authorization auth, RepoKey repo) {
        github.request(api.put(auth.requireHeader(), repo.owner(), repo.name()));
    }

    public void remove(Authorization auth, RepoKey repo) {
        github.request(api.delete(auth.requireHeader(), repo.owner(), repo.name()));
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

        @DELETE("/user/starred/{owner}/{repo}")
        Call<Void> delete(
            @Header("Authorization") String authorization,
            @Path("owner") String owner,
            @Path("repo") String repoName);
    }
}
