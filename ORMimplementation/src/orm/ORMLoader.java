package orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import annotations.PrimaryKey;
import annotations.Table;
import connector.CriteriaSet;
import connector.DatabaseConnector;
import connector.Pair;
import orm.TableData;
import exception.AutoIncrementWrongTypeException;
import exception.CommunicationException;
import exception.DbDriverNotFound;


public class ORMLoader {
 private final DatabaseConnector dbc;
 
 public ORMLoader(DatabaseConnector dbc) {
	 this.dbc=dbc;
 }
 
 public List<Object> get(Class<?> class_name,String field,String match) throws DbDriverNotFound, CommunicationException{
	//TableHierarchyData hierarchy;
	for(Annotation a:class_name.getAnnotations()) {
		if(a instanceof Table && !dbc.checkTable(((Table) a).name())) {
		      this.createTableHierarchyTables(class_name);
		}
	}
	return null;	 
 }
 
 
 /**
  * Parse the table/class hierarchy and compositional structure
  * @param t table to be used as a starting point for the parsing / structure mapped to this table
  * @param class_name class mapped to the table
  * @return hierarchical metadata structure
  */
 /*private TableHierarchyData  structuretablesuper(Class<?> class_name,TableData composition) {
	 Table t=ORMConverter.getTable(class_name);
       	 Table table_super=null,taux=t;
	 //PrimaryKey primary_super=null,primary_current=null;
	 Class<?> current_class=class_name;
	 TableData tab_sup_data=null,tab_cur_data=null;
	 List<TableData> hierarchy=new ArrayList<TableData> ();
	 List<TableHierarchyData> foreign_table=null;
	 while(!current_class.equals(Object.class)&&taux!=null) {
		 tab_sup_data=null;
		 Class<?> super_cls=current_class.getSuperclass();
		 table_super=ORMConverter.getTable(super_cls);
		 if(table_super!=null){
			 tab_sup_data=ORMConverter.convertTable(super_cls);
		 }
		 tab_cur_data=ORMConverter.convertTable(current_class);
		 try {
			if(table_super!=null&&dbc.checkTable(table_super.name()))
				this.updateTableForeignKey(tab_cur_data,tab_sup_data);
			if(!dbc.checkTable(taux.name()))
			      	if(tab_sup_data==null&&composition!=null)
			      	      this.createTable(tab_cur_data,composition);
			      	else 
			      	      this.createTable(tab_cur_data,tab_sup_data);
		} catch (DbDriverNotFound | CommunicationException e) {
			e.printStackTrace();
		}
		 
		 for(ColumnData cd:tab_cur_data.lcd) {
		       	 Type foreign_oneto=null;
			 if(cd.otm!=null) {
				 Type list_oneto=cd.f.getGenericType();
				 if(list_oneto instanceof ParameterizedType) {
					 ParameterizedType paramtype=(ParameterizedType) list_oneto;
					 if(((ParameterizedType) list_oneto).getActualTypeArguments().length>0)
						 foreign_oneto=paramtype.getActualTypeArguments()[0];
				 }	 
			 }
			 if(cd.oto!=null) {
			       foreign_oneto=cd.f.getGenericType();
			 }
			 if(foreign_oneto!=null) {
			       if(foreign_table==null)
					 foreign_table=new ArrayList<TableHierarchyData>();
			       foreign_table.add(this.structuretablesuper( (Class<?>) foreign_oneto,tab_cur_data));
				 System.out.println((Class<?>) foreign_oneto);
				 continue; 
			 }
		 }
		 
		 if(table_super!=null){
			 current_class=super_cls;
			 taux=table_super;
			 hierarchy.add(tab_sup_data);
		 }
			 //this.structuretablesuper(table_super, super_cls);
		 else break;
		 
	 }
	return new TableHierarchyData(hierarchy, ORMConverter.convertTable(class_name),foreign_table);
 }*/

 
 private void createTable(TableData current_table,TableData super_table) {
	 //System.out.println(table_current+" "+key_current+" "+foreign_table+" "+foreign_key);
	 String table_current_name=current_table==null?"":current_table.table.name();
	 String key_current_name=current_table==null?"":current_table.pk==null?"":current_table.pk.name();
	 String foreign_table_name=super_table==null?"":super_table.table.name();
	 String foreign_key_name=super_table==null?"":super_table.pk==null?"":super_table.pk.name();
	 if(current_table!=null&&current_table.pk!=null&&current_table.pk.autoincrement()&&!(current_table.pk_field.getType().equals(int.class)||current_table.pk_field.getType().equals(Integer.class)))
			throw new AutoIncrementWrongTypeException(current_table.table.name()+"."+current_table.pk.name());
	 if(super_table!=null&&super_table.pk!=null&&super_table.pk.autoincrement()&&!(super_table.pk_field.getType().equals(int.class)||super_table.pk_field.getType().equals(Integer.class)))
			throw new AutoIncrementWrongTypeException(super_table.table.name()+"."+super_table.pk.name());
	 dbc.createTable(current_table,super_table);
	 //System.out.println("Create table "+table_current_name+" primary key: "+key_current_name+" with foreign table: "+foreign_table_name+" foreign key: "+foreign_key_name);
 }
 
 private void updateTableForeignKey(TableData current,TableData foreign) {
	 dbc.updateTableForeignKey(current,foreign);
 }
  
 /**
  * 
  *  Creates a CriteriaSet used to apply a select on the specified table
  * @param table Classs which maps to a table to which the select is applied to
  * @return CriteriaSet
 * @throws CommunicationException 
 * @throws DbDriverNotFound 
  */
 public CriteriaSet setCriteria(Class<?> table) throws DbDriverNotFound, CommunicationException {
	 TableData td=ORMConverter.convertTable(table);
	 TableHierarchyData hierarchy=this.createTableHierarchyTables(table);
	 return dbc.setCriteria(td,hierarchy);
 }
 
 
 
 /**
  * ethod used to insert an object into the database with a provided foreign key for composition
  * @param o Object to insert
  * @return inserted ID
 * @throws CommunicationException 
 * @throws DbDriverNotFound 
  */
 public int insert(Object o) throws DbDriverNotFound, CommunicationException {
       return this.insert(o,null,null);
 }
 
 /**
  * Method used to insert an object into the database with a provided foreign key for composition
  * @param o Object to insert
  * @param foreign key for composition relationship mapping
  * @return inserted ID
 * @throws CommunicationException 
 * @throws DbDriverNotFound 
  */
 private int insert(Object o,Pair<PrimaryKey,Object> foreign,TableHierarchyValue hierarchy) throws DbDriverNotFound, CommunicationException {
	 TableHierarchyData thd=this.createTableHierarchyTables(o.getClass());
	 if( hierarchy == null)
	       hierarchy=ORMConverter.convertTableHierarchyValue(thd,o);
	 PrimaryKey last_pk=null;
	 Object last_val=null;
	 List<Pair<Class<?>,Object>> insert_ids_hie=new LinkedList<Pair<Class<?>,Object>>();
	 if(hierarchy.hierarchy!=null) {
	       for(int i=hierarchy.hierarchy.size()-1;i>=0;i--) {
		     TableValue tv=hierarchy.hierarchy.get(i);     
		     last_val=this.dbc.insert(o,tv,new Pair<PrimaryKey,Object>(last_pk,last_val),null);
		     last_pk=tv.pk;
		     insert_ids_hie.add(new Pair<Class<?>,Object>(tv.class_name,last_val));
	       }
	 }
	 int insert_val=this.dbc.insert(o,hierarchy.current,new Pair<PrimaryKey,Object>(last_pk,last_val),foreign);
	 //System.out.println(insert_val);
	 for(List<TableHierarchyValue> thvi:hierarchy.foreign_hie) 
	       for(TableHierarchyValue thvj:thvi) {
		     Pair<PrimaryKey,Object> fore_val=ORMConverter.getForeignKeyForColumn(insert_ids_hie, thd.hierarchy, thvj.current.class_name);
		     if(fore_val==null) 
			   fore_val=new Pair<PrimaryKey,Object>(hierarchy.current.pk,insert_val);
		     	int fore_id=this.insert(thvj.current.value,fore_val,thvj);
		     	//System.out.println(thvj.current.table.name()+" insert id "+fore_id);
	       	}	 
	 return insert_val;
 }

 
 
 private TableHierarchyData createTableHierarchyTables(Class<?> table_class) throws DbDriverNotFound, CommunicationException {
       TableHierarchyData thd = ORMConverter.extractHierarchicalData(table_class);
       this.parseAndCreateTables(thd,null);
       return thd;
       
 }
 
 private void parseAndCreateTables(TableHierarchyData thd,TableData composition) throws DbDriverNotFound, CommunicationException {
       TableData prev=null;
       for(int i=thd.hierarchy.size()-1;i>=0;i--) {
	     TableData curr=thd.hierarchy.get(i);
	     if(!dbc.checkTable(curr.table.name()))
		   this.createTable(curr, prev);
	     if(prev!=null&&dbc.checkTable(prev.table.name()))
			this.updateTableForeignKey(curr,prev);
	     prev=thd.hierarchy.get(i);
       }
       if(!dbc.checkTable(thd.current.table.name()))
	     if(prev==null&&composition!=null)
		   this.createTable(thd.current, composition);
	     else 
		   this.createTable(thd.current, prev);
       if(prev!=null&&dbc.checkTable(prev.table.name()))
		this.updateTableForeignKey(thd.current,prev);
       if(thd.foreign_hie!=null)
	     for(TableHierarchyData thdi:thd.foreign_hie) {
		   TableData tabmain=ORMConverter.getForeignTableMainFromFullHierarchy(thd.current,thd.hierarchy, thdi.current);
		   this.parseAndCreateTables(thdi,tabmain);}
 }
 
 public void dropTable(Class<?> table) {
       TableHierarchyData thd=ORMConverter.extractHierarchicalData(table);
       List<Table> list_drops=ORMConverter.getRelatingTables(thd);
       dbc.dropTable(list_drops);
 }
 
}
