package connector;

import java.util.List;

import annotations.Table;
import exception.CommunicationException;
import exception.ConstructorException;
import exception.DbDriverNotFound;
import exception.NoSuchColumnException;
import orm.TableData;
import orm.TableHierarchyData;

public class CriteriaSetMariaDb extends CriteriaSet {
	
	public CriteriaSetMariaDb(DatabaseConnector dbc, TableData table, TableHierarchyData hierarchy) {
		super(dbc, table, hierarchy);
	}
	
	public void gt(String column,Object o) {
		String quote="";
		if(o instanceof String)
			quote="'";
		Table t=getTableColumn(hierarchy,column);
		if(t!=null)
			this.crits.add(new Criteria(t,"."+column+" > "+quote+o.toString()+quote));
		else throw new NoSuchColumnException(column);
	}

	public void lt(String column,Object o) {
		String quote="";
		if(o instanceof String)
			quote="'";
		Table t=getTableColumn(hierarchy,column);
		if(t!=null)
			this.crits.add(new Criteria(t,"."+column+" < "+quote+o.toString()+quote));	
		else throw new NoSuchColumnException(column);
	}

	public void eq(String column,Object o) {
		String quote="";
		if(o instanceof String)
			quote="'";
		Table t=getTableColumn(hierarchy,column);
		if(t!=null)
			this.crits.add(new Criteria(t,"."+column+" = "+quote+o.toString()+quote));	
		else throw new NoSuchColumnException(column);
	}

	public void like(String column,String s) {
		Table t=getTableColumn(hierarchy,column);
		if(t!=null)
			this.crits.add(new Criteria(t,"."+column+" like '"+s+"' "));
		else throw new NoSuchColumnException(column);
	}

	public void orderAsc(String column) {
		Table t=getTableColumn(hierarchy,column);
		if(t!=null) {
			this.order= column+" ASC ";
			this.order_table=t;}
		else throw new NoSuchColumnException(column);
	}

	public void orderDesc(String column) {
		Table t=getTableColumn(hierarchy,column);
		if(t!=null) {
			this.order= column+" DESC ";
			this.order_table=t;}
		else throw new NoSuchColumnException(column);
	}

	public List<Object> extract() throws ConstructorException, SecurityException, DbDriverNotFound, CommunicationException {
		return dbc.select(this);
	}
}
