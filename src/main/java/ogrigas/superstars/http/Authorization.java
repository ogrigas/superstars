package ogrigas.superstars.http;

import spark.Request;

public class Authorization {
    private final String header;

    private Authorization(String header) {
        this.header = "".equals(header) ? null : header;
    }

    public static Authorization fromHeader(String header) {
        return new Authorization(header);
    }

    public static Authorization from(Request request) {
        return fromHeader(request.headers("Authorization"));
    }

    public boolean provided() {
        return header != null;
    }

    public String header() {
        if (header == null) {
            throw new ClientError("Authorization header required");
        }
        return header;
    }

    public String optionalHeader() {
        return header;
    }
}
