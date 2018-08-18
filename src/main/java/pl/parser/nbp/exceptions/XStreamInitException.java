package pl.parser.nbp.exceptions;

public class XStreamInitException extends Exception {
    public XStreamInitException() {
    }

    public XStreamInitException(String message) {
        super(message);
    }

    public XStreamInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public XStreamInitException(Throwable cause) {
        super(cause);
    }
}
