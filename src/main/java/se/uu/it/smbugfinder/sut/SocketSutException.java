package se.uu.it.smbugfinder.sut;

public class SocketSutException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SocketSutException(String message) {
		super(message);
	}
	
	public SocketSutException(String message, Exception cause) {
		super(message, cause);
	}
	
}
