package ogrigas.superstars.http;

public class Authorization {
    private final String header;

    private Authorization(String header) {
        this.header = "".equals(header) ? null : header;
    }

    public static Authorization fromHeader(String header) {
        return new Authorization(header);
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
}
