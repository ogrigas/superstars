package ogrigas.superstars.github;

import ogrigas.superstars.http.Authorization;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.concurrent.CompletableFuture;

public class GithubUserStarredRepos {

    private final Github github;
    private final Api api;

    public GithubUserStarredRepos(Github github) {
        this.github = github;
        this.api = github.proxy(Api.class);
    }

    public CompletableFuture<Boolean> contains(Authorization auth, RepoKey repo) {
        return api.get(auth.requireHeader(), repo.owner(), repo.name())
            .whenComplete(github::handleErrors)
            .thenApply(response -> response.code() == 204);
    }

    public CompletableFuture<Void> add(Authorization auth, RepoKey repo) {
        return api.put(auth.requireHeader(), repo.owner(), repo.name())
            .whenComplete(github::handleErrors)
            .thenApply(Response::body);
    }

    public CompletableFuture<Void> remove(Authorization auth, RepoKey repo) {
        return api.delete(auth.requireHeader(), repo.owner(), repo.name())
            .whenComplete(github::handleErrors)
            .thenApply(Response::body);
    }

    private interface Api {

        @GET("/user/starred/{owner}/{repo}")
        CompletableFuture<Response<Void>> get(
            @Header("Authorization") String authorization,
            @Path("owner") String owner,
            @Path("repo") String repoName);

        @PUT("/user/starred/{owner}/{repo}")
        CompletableFuture<Response<Void>> put(
            @Header("Authorization") String authorization,
            @Path("owner") String owner,
            @Path("repo") String repoName);

        @DELETE("/user/starred/{owner}/{repo}")
        CompletableFuture<Response<Void>> delete(
            @Header("Authorization") String authorization,
            @Path("owner") String owner,
            @Path("repo") String repoName);
    }
}
