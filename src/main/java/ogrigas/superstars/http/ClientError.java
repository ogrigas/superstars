package ogrigas.superstars.http;

public class ClientError extends RuntimeException {
    public ClientError(String message) {
        super(message);
    }
}
