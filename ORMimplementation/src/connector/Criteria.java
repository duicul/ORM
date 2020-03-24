package connector;

import java.util.ArrayList;
import java.util.List;

public abstract class Criteria {
	protected List<Pair<String,String>> rests;
	protected String order;
	public final DatabaseConnector dbc;
	public Criteria(DatabaseConnector dbc) {
		this.dbc=dbc;
		this.rests=new ArrayList<Pair<String,String>>();
	}
	public abstract void gt(Object o);
	public abstract void lt(Object o);
	public abstract void eq(Object o);
	public abstract void like(String s);
	public abstract void orderAsc();
	public abstract void orderDesc();
	
}
