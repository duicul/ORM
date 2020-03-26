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
import orm.TableHierarchyData;

public abstract class CriteriaSet {
	protected List<Criteria> crits;
	protected String order=null;
	protected Table order_table=null;
	public final DatabaseConnector dbc;
	public final TableData table;
	protected final TableHierarchyData hierarchy;
	
	public CriteriaSet(DatabaseConnector dbc,TableData table,TableHierarchyData hierarchy) {
		this.dbc=dbc;
		this.table=table;
		this.hierarchy=hierarchy;
		this.crits=new ArrayList<Criteria>();
	}
	
	public static Table getTableColumn(TableHierarchyData hierar,String Column) {
		TableData table_curr=hierar.current;
		if(table_curr.pk!=null&&table_curr.pk.name().equals(Column))
			return table_curr.table;
		for(ColumnData cd:table_curr.lcd)
			if(matchColumn(cd, Column))
				return table_curr.table;
		for(TableData td:hierar.hierarchy)
			for(ColumnData cd:td.lcd)
				if(matchColumn(cd, Column))
					return td.table;
		if(hierar.foreign_hie!=null)
			for(TableHierarchyData thd:hierar.foreign_hie) {
				Table t=getTableColumn(thd,Column);
				if(t!=null)
					return t;
			}
		return null;
	}
	
	public static boolean matchColumn(ColumnData cd,String Column) {
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
		if(this.order_table==null||this.order==null)
			return "";
		return " ORDER BY "+this.order_table.name()+"."+this.order;
	}
	
}
