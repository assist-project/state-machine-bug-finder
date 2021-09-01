package se.uu.it.bugfinder.encoding;

public class InvalidLabelException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidLabelException(String label) {
		super("Label \"" + label + "\" is invalid. ");
	}
	
	public InvalidLabelException(String label, Throwable exception) {
		super("Label \"" + label + "\" is invalid.", exception);
	}
}
