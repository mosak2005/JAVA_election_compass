package pl.project.sejm;

public class SejmApiException extends Exception {
    public SejmApiException(String message) { super(message); }
    public SejmApiException(String message, Throwable cause) { super(message, cause); }
    public SejmApiException(Throwable cause) { super(cause); }

    public boolean isNetworkError() {
        Throwable c = getCause();
        while (c != null) {
            if (c instanceof java.net.ConnectException
                    || c instanceof java.net.UnknownHostException
                    || c instanceof java.net.SocketTimeoutException
                    || c instanceof java.net.http.HttpTimeoutException) return true;
            c = c.getCause();
        }
        return false;
    }
}

