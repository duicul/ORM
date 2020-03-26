package exception;

public class AutoIncrementValueException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8579329419518829532L;
	public AutoIncrementValueException(String primarykey) {
		super("Autoincrement is set to false and no value is provided for PrimaryKey: "+primarykey+" or given value is unsupported (<=0)");
	}
}
