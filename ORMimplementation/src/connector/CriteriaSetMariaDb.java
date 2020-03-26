package connector;

import java.util.List;

import exception.CommunicationException;
import exception.ConstructorException;
import exception.DbDriverNotFound;
import orm.ColumnData;
import orm.TableData;

public class CriteriaSetMariaDb extends CriteriaSet {
	
	public CriteriaSetMariaDb(DatabaseConnector dbc, TableData table, List<TableData> hierarchy) {
		super(dbc, table, hierarchy);
	}
	
	public void gt(String column,Object o) {
		String quote="";
		if(o instanceof String)
			quote="'";
		this.crits.add(new Criteria(this.getTableColumn(column),"."+column+" > "+quote+o.toString()+quote));	
	}

	public void lt(String column,Object o) {
		String quote="";
		if(o instanceof String)
			quote="'";
		this.crits.add(new Criteria(this.getTableColumn(column),"."+column+" < "+quote+o.toString()+quote));	
		
	}

	public void eq(String column,Object o) {
		String quote="";
		if(o instanceof String)
			quote="'";
		this.crits.add(new Criteria(this.getTableColumn(column),"."+column+" = "+quote+o.toString()+quote));	
		
	}

	public void like(String column,String s) {
		this.crits.add(new Criteria(this.getTableColumn(column),"."+column+" like '"+s+"' "));		
	}

	public void orderAsc(String column) {
		this.order= column+" ASC ";
		this.order_table=this.getTableColumn(column);
	}

	public void orderDesc(String column) {
		this.order= column+" DESC ";
		this.order_table=this.getTableColumn(column);		
	}

	public List<Object> extract() throws ConstructorException, SecurityException, DbDriverNotFound, CommunicationException {
		return dbc.select(this);
	}
}
