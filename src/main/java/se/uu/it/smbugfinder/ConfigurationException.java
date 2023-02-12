package se.uu.it.smbugfinder;

/**
 * Used to signal bad arguments.
 */
public class ConfigurationException extends RuntimeException{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ConfigurationException(String message) {
		super(message);
	}
}
