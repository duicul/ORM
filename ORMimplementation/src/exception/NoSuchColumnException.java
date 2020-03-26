package exception;

public class NoSuchColumnException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1432938036477573138L;
	public NoSuchColumnException(String column) {
		super("No table found for column:"+column);
	}
}
