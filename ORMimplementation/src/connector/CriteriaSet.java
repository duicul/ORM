package connector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import annotations.PrimaryKey;
import annotations.Table;
import exception.CommunicationException;
import exception.ConstructorException;
import exception.DbDriverNotFound;
import exception.DeleteComposition;
import orm.ColumnData;
import orm.ORMConverter;
import orm.TableData;
import orm.TableHierarchyData;
import orm.TableValue;

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
	
	public void remove() throws ConstructorException, SecurityException, DbDriverNotFound, CommunicationException, DeleteComposition {
	      //List<TableData> hierar=ORMConverter.extractFullTableHierarchy(this.table.class_name);
	      //List<Table> hierar_table = hierar.stream().map(td->td.table).collect(Collectors.toList());
	      //List<Criteria> curr_crit=this.crits;
	      //this.dbc.delete(hierar, this.crits, this.table);
	      this.parse_composition(this.table,null,null,true);
	}
	
	public List<Object> extract() throws ConstructorException, SecurityException, DbDriverNotFound, CommunicationException, DeleteComposition{
	      return this.parse_composition(this.table,null,null,false);}
	
	public List<Object> parse_composition(TableData curr_table,PrimaryKey composition,Object val_comp,boolean remove) throws ConstructorException, SecurityException, DbDriverNotFound, CommunicationException, DeleteComposition{
	      List<TableData> hierar=ORMConverter.extractFullTableHierarchy(curr_table.class_name);
	      List<Table> hierar_table = hierar.stream().map(td->td.table).collect(Collectors.toList());
	      List<Criteria> curr_crit=this.filterCriteriasForTableHierarchy(hierar_table);
	      String curr_order=this.filterOrderForTableHierarchy(hierar_table);
	      if(composition!=null&&val_comp!=null)
		    curr_crit.add(new Criteria(curr_table.table,"."+composition.name()+" = "+val_comp));
	      List<Object> ret;
	      ret=dbc.projection(hierar,curr_crit,curr_table,curr_order,false);
	      List<Pair<Object,TableData>> remove_list=new ArrayList<Pair<Object,TableData>>();
	      List<Object> remove_list_obj=new ArrayList<Object>();
	      if(ret!=null)
	      for(Object ret_obj:ret){
		    for(TableData tdi:hierar)
		    for(ColumnData cd:tdi.lcd) {
			  if(cd.f!=null) {
				List<Object> obj_val=null;
				if(cd.otm!=null||cd.oto!=null) {
				      Class<?> fore_cls=ORMConverter.extractOneToReference(cd);
				      TableData fore_table=ORMConverter.convertTable(fore_cls);
				      TableData main_table_fore=ORMConverter.getForeignTableMainFromFullHierarchy(hierar, fore_table);
				      Object curr_table_pk_val=null;
				    try {
					  curr_table_pk_val = curr_table.pk_field.get(ret_obj);
				    } catch (IllegalArgumentException | IllegalAccessException e) {
					  // TODO Auto-generated catch block
					e.printStackTrace();
				    }
				      obj_val=this.parse_composition(fore_table,main_table_fore.pk,curr_table_pk_val,remove);
				      //ORMConverter.getForeignTableFromHierarchy(curr_table, hierar,fore_table);
				      
				if(obj_val==null) {
				      List<TableData> hierar1=ORMConverter.extractFullTableHierarchy(fore_table.class_name);
				      List<Table> hierar_table1 = hierar1.stream().map(td->td.table).collect(Collectors.toList());
				      List<Criteria> curr_crit1=this.filterCriteriasForTableHierarchy(hierar_table1);
				      if(curr_crit1.size()!=0)
					    if(!remove_list_obj.contains(ret_obj)) {
						  remove_list_obj.add(ret_obj);
						  remove_list.add(new Pair<Object,TableData>(ret_obj,curr_table));
					    }
				      continue;}
			  	}
				if(cd.oto!=null) {
				      if(obj_val.size()>0)
					  try {
						cd.f.set(ret_obj,obj_val.get(0));
					  } catch (IllegalArgumentException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					  }
				}
				if(cd.otm!=null) {
				      try {
					  cd.f.set(ret_obj,obj_val);
				    } catch (IllegalArgumentException | IllegalAccessException e) {
					  // TODO Auto-generated catch block
					e.printStackTrace();
				    }
				}
			  }	  
		    }
	      }
	      for(Object o:remove_list_obj)
		    ret.remove(o);
	      if(remove) {
		    for(Pair<Object,TableData> tdi:remove_list) {
			  TableValue tvi=ORMConverter.convertTableValue(tdi.r, tdi.l);
			  List<TableData> hierar1=ORMConverter.extractFullTableHierarchy(tdi.r.class_name);
			  List<Table> hierar_table1 = hierar1.stream().map(td->td.table).collect(Collectors.toList());
			  List<Criteria> curr_crit1=this.filterCriteriasForTableHierarchy(hierar_table1);
			  
			  curr_crit1.add(new Criteria(tdi.r.table,"."+tvi.pk.name()+" = "+tvi.pk_val));
			  dbc.projection(hierar1,curr_crit1,tdi.r,null,true);}
			   for(Criteria c:this.crits) {
				 boolean found=false;
				 for(TableData td:hierar)
				       if(c.getCriteriaTable().equals(td.table)) {
					     found=true;
					     break;}
				// if(!found)
				//       throw new DeleteComposition(c.getCriteria());
			   }
			   //ret=dbc.projection(hierar,curr_crit,curr_table,curr_order,false);
			   if(this.filterCriteriasForTableHierarchy(hierar_table).size()>0)
		           dbc.projection(hierar,curr_crit,curr_table,curr_order,true);
		          }
	      if(ret!=null&&ret.size()==0)
		    return null;
	      return ret;
	}
	
	/**
	 *  Filter the criteria set for a specific current table and its hierarchy
	 * @param current table used to filter
	 * @param hierarchy the current table's hierarchy
	 * @return List of filtered Criterias
	 */
	private List<Criteria> filterCriteriasForTableHierarchy(List<Table> hierarchy) {
	      return this.crits.stream().filter(c->{
		    	if(hierarchy==null)
		    	      return false;
	      		for(Table ti:hierarchy)
	      		      if(ti.name().equals(c.getCriteriaTable().name()))
	      			    return true;
	      		return false;
	      	}
			).collect(Collectors.toList());
	      
	}
	
	/**
	 * Verify if order matches the current table hierarchy
	 * @param current table used to filter
	 * @param hierarchy the current table's hierarchy
	 * @return the order query if match otherwise <b>null</b>
	 */
	private String filterOrderForTableHierarchy(List<Table> hierarchy) {
	      if(hierarchy==null)
		    return null;
	      for(Table t:hierarchy)
		    if(this.order_table!=null&&this.order_table.name().equals(t.name()))
			    this.getOrder();
	      return null;	      
	}
	
	public List<Criteria> getCiterias(){
		return this.crits;
	}
	
	public String getOrder() {
		if(this.order_table==null||this.order==null)
			return "";
		return " ORDER BY "+this.order_table.name()+"."+this.order;
	}
	
}
