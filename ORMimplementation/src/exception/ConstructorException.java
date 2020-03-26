package exception;

public class ConstructorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1850849109436971936L;
	public ConstructorException() {
		super("Public constructor with no parameters needed");
	}
}
