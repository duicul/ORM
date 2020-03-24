package connector;

public class CriteriaMariaDB extends Criteria {

	public CriteriaMariaDB(DatabaseConnector dbc) {
		super(dbc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void gt(Object o) {
		this.rests.add(new Pair<String, String>(">",o.toString()));
		
	}

	@Override
	public void lt(Object o) {
		this.rests.add(new Pair<String, String>("<",o.toString()));
		
	}

	@Override
	public void eq(Object o) {
		this.rests.add(new Pair<String, String>("=",o.toString()));
		
	}

	@Override
	public void like(String s) {
		this.rests.add(new Pair<String, String>("like","'"+s+"'"));
		
	}

	@Override
	public void orderAsc() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void orderDesc() {
		// TODO Auto-generated method stub
		
	}

}
