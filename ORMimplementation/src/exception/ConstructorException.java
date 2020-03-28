package exception;

public class ConstructorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1850849109436971936L;
	public ConstructorException(String class_name) {
		super("Public constructor with no parameters needed in class "+class_name);
	}
}
