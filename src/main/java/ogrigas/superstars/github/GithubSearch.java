package ogrigas.superstars.github;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.net.URL;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class GithubSearch {

    private final GithubClient client;
    private final Api api;

    public GithubSearch(GithubClient client) {
        this.client = client;
        this.api = client.proxy(Api.class);
    }

    public List<RepoSearchItem> topActiveRepositories(RepoSearchQuery query) {
        String q = query.term() + " language:" + query.language();
        Call<Api.Results> apiCall = api.searchRepositories(q, Api.Sort.stars, Api.Order.desc, query.limit());
        Api.Results results = client.request(apiCall).body();
        return results == null ? emptyList() : results.items.stream()
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
