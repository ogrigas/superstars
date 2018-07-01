package ogrigas.superstars.github;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.toMap;

/**
 * Parses HTTP "Link" headers (RFC 5988 Web Linking), at least the minimum needed for GithubHttp API pagination.
 * "Isn't there a dedicated Java library for that?" you might ask. Not as of 2018.
 * Even frickin' Elm has one. Sad :-(
 */
class Links {

    private static final Pattern LINK_SEPARATOR = Pattern.compile("\\s*,\\s*");

    private static final Pattern LINK_WITH_REL_PATTERN = Pattern.compile(
        "^<(?<HREF>[^>]+)>.*;\\s*rel=\"(?<REL>[^\"]+)\".*", CASE_INSENSITIVE);

    private final Map<String, String> relMap;

    private Links(Map<String, String> relMap) {
        this.relMap = relMap;
    }

    public static Links parse(String header) {
        return new Links(header == null || header.isEmpty() ? emptyMap() : parseRelMap(header));
    }

    private static Map<String, String> parseRelMap(String header) {
        return LINK_SEPARATOR.splitAsStream(header.trim())
            .map(LINK_WITH_REL_PATTERN::matcher)
            .filter(Matcher::matches)
            .collect(toMap(m -> m.group("REL").toLowerCase(), m -> m.group("HREF")));
    }

    public Optional<URL> rel(String relValue) {
        String href = relMap.get(relValue.toLowerCase());
        return Optional.ofNullable(href).flatMap(Links::toUrl);
    }

    private static Optional<URL> toUrl(String href) {
        try {
            return Optional.of(new URL(href));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }
}
