package ogrigas.superstars.github;

import ogrigas.superstars.http.Authorization;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

import java.net.URL;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class GithubSearch {

    private final Github github;
    private final Api api;

    public GithubSearch(Github github) {
        this.github = github;
        this.api = github.proxy(Api.class);
    }

    public List<RepoSearchItem> topActiveRepositories(Authorization auth, RepoSearchQuery query) {
        Response<Api.Results> response = github.request(api.searchRepositories(
            auth.optionalHeader(),
            query.term() + " language:" + query.language(),
            Api.Sort.stars,
            Api.Order.desc,
            query.limit()
        ));
        return response.body() == null ? emptyList() : response.body().items.stream()
            .map(item -> RepoSearchItem.builder()
                .owner(item.owner != null ? item.owner.login : null)
                .name(item.name)
                .description(item.description)
                .license(item.license != null ? item.license.name : null)
                .url(item.url)
                .starCount(item.stargazers_count)
                .build())
            .collect(toList());
    }

    private interface Api {

        @GET("/search/repositories")
        Call<Results> searchRepositories(
            @Header("Authorization") String authorization,
            @Query("q") String query,
            @Query("sort") Sort sort,
            @Query("order") Order order,
            @Query("per_page") int pageSize);

        enum Sort {
            stars,
            forks,
            updated
        }

        enum Order {
            asc,
            desc
        }

        class Results {
            List<Item> items;
        }

        class Item {
            String name;
            String description;
            Owner owner;
            License license;
            URL url;
            int stargazers_count;
        }

        class Owner {
            String login;
        }

        class License {
            String name;
        }
    }
}
