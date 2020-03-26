package connector;

import java.util.ArrayList;
import java.util.List;

import annotations.Table;
import exception.CommunicationException;
import exception.ConstructorException;
import exception.DbDriverNotFound;
import exception.NoSuchColumnException;
import orm.ColumnData;
import orm.TableData;

public abstract class CriteriaSet {
	protected List<Criteria> crits;
	protected String order;
	protected Table order_table;
	public final DatabaseConnector dbc;
	public final TableData table;
	protected final List<TableData> hierarchy;
	
	public CriteriaSet(DatabaseConnector dbc,TableData table,List<TableData> hierarchy) {
		this.dbc=dbc;
		this.table=table;
		this.hierarchy=hierarchy;
		this.crits=new ArrayList<Criteria>();
	}
	
	protected Table getTableColumn(String Column) {
		if(this.table.pk!=null&&this.table.pk.name().equals(Column))
			return this.table.table;
		for(ColumnData cd:this.table.lcd)
			if(this.matchColumn(cd, Column))
				return this.table.table;
		for(TableData td:hierarchy)
			for(ColumnData cd:td.lcd)
				if(this.matchColumn(cd, Column))
					return td.table;
		throw new NoSuchColumnException(Column);
	}
	
	protected boolean matchColumn(ColumnData cd,String Column) {
		if(cd.col!=null&&cd.col.name().equals(Column))
			return true;
		return false;
	}
	
	public abstract void gt(String column,Object o);
	public abstract void lt(String column,Object o);
	public abstract void eq(String column,Object o);
	public abstract void like(String column,String s);
	public abstract void orderAsc(String column);
	public abstract void orderDesc(String column);
	public abstract List<Object> extract() throws ConstructorException, SecurityException, DbDriverNotFound, CommunicationException;
	
	public List<Criteria> getCiterias(){
		return this.crits;
	}
	
	public String getOrder() {
		return " ORDER BY "+this.order_table.name()+"."+this.order;
	}
	
}
