package se.uu.it.smbugfinder.sut;

public class SocketSUTException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SocketSUTException(String message) {
        super(message);
    }

    public SocketSUTException(String message, Exception cause) {
        super(message, cause);
    }

}
