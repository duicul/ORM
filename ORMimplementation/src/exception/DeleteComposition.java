package exception;

public class DeleteComposition extends Exception {

      /**
       * 
       */
      private static final long serialVersionUID = 6654775950026570408L;
      public DeleteComposition(String criteria) {
		super("For deletion use criterias which hierarhichaly relate to the current table, wrong criteria => "+criteria);
	}
}
