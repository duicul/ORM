package exception;

public class AutoIncrementWrongTypeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8910718255798247989L;

	public AutoIncrementWrongTypeException(String column) {
		super("Primary key "+column+" is autoincremented and needs to be an integer ");
	}

}
