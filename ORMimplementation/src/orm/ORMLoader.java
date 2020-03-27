package orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import annotations.Column;
import annotations.OneToMany;
import annotations.OneToOne;
import annotations.PrimaryKey;
import annotations.Table;
import connector.CriteriaSet;
import connector.DatabaseConnector;
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
	 TableHierarchyData hierarchy;
	for(Annotation a:class_name.getAnnotations()) {
		if(a instanceof Table && !dbc.checkTable(((Table) a).name())) {
			hierarchy=this.structuretablesuper((Table) a, class_name);
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
 private TableHierarchyData  structuretablesuper(Table t,Class<?> class_name) {
	 Table table_super=null,taux=t;
	 PrimaryKey primary_super=null,primary_current=null;
	 Class<?> current_class=class_name;
	 TableData tab_sup_data=null,tab_cur_data=null;
	 List<TableData> hierarchy=new ArrayList<TableData> ();
	 List<TableHierarchyData> foreign_table=null;
	 while(!current_class.equals(Object.class)&&taux!=null) {
		 tab_sup_data=null;
		 Class<?> super_cls=current_class.getSuperclass();
		 table_super=this.getTable(super_cls);
		 if(table_super!=null){
			 primary_super=this.getPrimaryKey(super_cls);
			 tab_sup_data=this.convertTable(super_cls,table_super);
		 }
		 primary_current=this.getPrimaryKey(current_class);
		 tab_cur_data=this.convertTable(current_class,taux);
		 try {
			if(table_super!=null&&dbc.checkTable(table_super.name()))
				this.updateTableForeignKey(tab_cur_data,tab_sup_data);
			if(!dbc.checkTable(taux.name()))
				this.createTable(tab_cur_data,tab_sup_data);
		} catch (DbDriverNotFound | CommunicationException e) {
			e.printStackTrace();
		}
		 
		 for(ColumnData cd:tab_cur_data.lcd) {
			 if(cd.otm!=null) {
				 Type foreign_oneto=null;
				 Type list_oneto=cd.f.getGenericType();
				 if(list_oneto instanceof ParameterizedType) {
					 ParameterizedType paramtype=(ParameterizedType) list_oneto;
					 if(((ParameterizedType) list_oneto).getActualTypeArguments().length>0)
						 foreign_oneto=paramtype.getActualTypeArguments()[0];
				 }
				 if(foreign_table==null)
					 foreign_table=new ArrayList<TableHierarchyData>();
				 foreign_table.add(this.structuretablesuper(this.getTable((Class<?>) foreign_oneto), (Class<?>) foreign_oneto));
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
	return new TableHierarchyData(hierarchy, this.convertTable(class_name,t),foreign_table);
 }

 
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
	 System.out.println("Create table "+table_current_name+" primary key: "+key_current_name+" with foreign table: "+foreign_table_name+" foreign key: "+foreign_key_name);
 }
 
 private void updateTableForeignKey(TableData current,TableData foreign) {
	 dbc.updateTableForeignKey(current,foreign);
 }
 /**
  *  Extract the metadata for a given table
  * @param table for which the metadata is extracted
  * @param t class mapped to the table
  * @return metadata structure TableData
  */
 public TableData convertTable(Class<?> table,Table t){
	 List<ColumnData> lcd=new ArrayList<ColumnData>();
	 PrimaryKey pk=null;
	 Field pk_field=null;
	 for(Field f:table.getDeclaredFields()) {
		 Type type=null;
		 Column c=null;
		 OneToMany otm=null;
		 OneToOne oto=null;
		 type=f.getGenericType();
		 for(Annotation a:f.getAnnotations()) {
			 if(a instanceof Column) {
				 c=(Column) a;
				 continue;}
		 	 if(a instanceof OneToMany) {
		 		 otm=(OneToMany) a;
		 		 continue;}
		 	 if(a instanceof OneToOne) {
		 		 oto=(OneToOne) a;
		 		 continue;}
		 	if(a instanceof PrimaryKey) {
		 		 pk_field=f;
		 		 pk=(PrimaryKey) a;
		 		 continue;}
		 }
		 lcd.add(new ColumnData(c, otm, oto,f));
	 }	 
	 return new TableData(lcd,t,pk, pk_field, table);
 }
 /**
  * Used to extract the foreign tables' TableData structure for a given primary table
  * @param primary main table's TableData structure
  * @param foreign table metadata both compositional and hierarchical 
  * @return List of the corresponding table's metadata TableData
  */
 private List<TableHierarchyValue> extractOneToValue(TableData primary,List<TableHierarchyData> foreign,Object o) {
	 List<ColumnData> onetomany=new ArrayList<ColumnData>(),onetoone=new ArrayList<ColumnData>();
	 List<TableData> rettabd=new ArrayList<TableData>();
	 List<TableHierarchyValue> ot_hie_val=new LinkedList<TableHierarchyValue>();
	 for(ColumnData cd:primary.lcd) {
		if(cd.otm!=null)
			onetomany.add(cd);
		if(cd.oto!=null)
			onetoone.add(cd);
	 }
	 if(foreign==null)
	       return null;
	 for(TableHierarchyData thd:foreign) {
		 TableData td=thd.current;
		 for(ColumnData oto:onetoone)
			 if(oto.f.getGenericType().equals(td.class_name)) {
			       Object obj=null;
			       try {
				    obj=oto.f.get(o);
			      } catch (IllegalArgumentException | IllegalAccessException e) {
				    e.printStackTrace();}
				    if(obj!=null) {
					  TableHierarchyValue thv=this.convertTableHierarchyValue(thd, obj);
					  if(thv!=null)
						ot_hie_val.add(thv);
				    }
			      }
		 for(ColumnData otm:onetomany) {
			 Type list_oneto=otm.f.getGenericType();
			 Type foreign_oneto=null;
			 if(list_oneto instanceof ParameterizedType) {
				 ParameterizedType paramtype=(ParameterizedType) list_oneto;
				 if(((ParameterizedType) list_oneto).getActualTypeArguments().length>0)
					 foreign_oneto=paramtype.getActualTypeArguments()[0];}
			 
			 if(foreign_oneto!=null&&foreign_oneto.equals(td.class_name)) {
				List<Object> val_list_fore=null;
				try {
				val_list_fore=(List<Object>) otm.f.get(o);
				} catch (IllegalArgumentException | IllegalAccessException e) {
				      e.printStackTrace();
				}
				if(val_list_fore!=null)
				      for(Object fore_val:val_list_fore) {
					    TableHierarchyValue thv=this.convertTableHierarchyValue(thd, fore_val);
					    if(thv!=null)
						  ot_hie_val.add(thv);
				      }
			 }
		 }
	 }
	 return ot_hie_val.size()==0?null:ot_hie_val; 
 }
 
 /**
  * Used to add the values to a tree metadata structure containing both hierarchical and compositional structure of a class
  * @param td Metadata tree structure containing the objects structure
  * @param o Value to be added
  * @return TableHierarchyValue containing the value added mapped to the metadata structure
  */ 
 public TableHierarchyValue convertTableHierarchyValue(TableHierarchyData td,Object o) {
       TableHierarchyValue thv=this.convertTableHierarchyValueClass(td, o);
       if(thv!=null) {
	     thv.addForeignHierarchy(this.extractOneToValue(td.current,td.foreign_hie, o));
	     for(TableData tdi:td.hierarchy)
		   thv.addForeignHierarchy(this.extractOneToValue(tdi,td.foreign_hie, o));}
       return thv;
 }
 
 
 /**
  * Used to add the values to a tree metadata structure for the <b>hierarchical</b> structure of a class
  * @param td Metadata tree structure containing the objects structure
  * @param o Value to be added
  * @return TableHierarchyValue containing the value added mapped to the metadata structure
  */
 private TableHierarchyValue convertTableHierarchyValueClass(TableHierarchyData td,Object o){
	 List<TableHierarchyValue> lthv_foreign=null;
	 List<TableValue> ltv_fore=null;
	 TableValue curr_value=this.convertTableValue(td.current,o);
	 
	 //Convert class hierarchy tables
	 for(TableData tdi:td.hierarchy) {		 
		 if(ltv_fore==null)
			 ltv_fore=new ArrayList<TableValue>();
		 TableValue tv_curr=this.convertTableValue(tdi,o);
		 if(tv_curr!=null)
			 ltv_fore.add(tv_curr);
	 }
	 return new TableHierarchyValue(ltv_fore,curr_value);
 }
 
 /**
  * Used to add the values into a TableData structure
  * @param td TableData structure containing table metadata mapped to a class
  * @param o Value to be added
  * @return new structure containing the metadata and the value for each field/column
  */
 public TableValue convertTableValue(TableData td,Object o){
	 /*if(!td.table.name().equals(this.getTable(o.getClass()).name()))
		 return null;*/
	 List<ColumnValue> lcv=new ArrayList<ColumnValue>();
	 Object pk_val=null;
	try {
		pk_val = td.pk_field.get(o);
	} catch (IllegalArgumentException | IllegalAccessException e1) {
	      e1.printStackTrace();
	      return null;
	}
	 for(ColumnData cd:td.lcd) {
			Object col_val=null;
			try {
				col_val=cd.f.get(o);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();}
			lcv.add(new ColumnValue(cd,col_val));	
		}
	return new TableValue(lcv,td,pk_val);
	 
 }
 
 
 /**
  * 
  *  Creates a CriteriaSet used to apply a select on the specified table
  * @param table Classs which maps to a table to which the select is applied to
  * @return CriteriaSet
  */
 public CriteriaSet setCriteria(Class<?> table) {
	 TableData td=this.convertTable(table, this.getTable(table));
	 TableHierarchyData hierarchy=this.structuretablesuper(td.table, table);
	 return dbc.setCriteria(td,hierarchy);
 }
 
 /**
  *  Used to extract the Table annotation from a Class
  * @param tab Classs which maps to a table 
  * @return Table annotation
  */
 
 public Table getTable(Class<?> tab) {
		 for(Annotation a:tab.getAnnotations())
			 if(a instanceof Table) 
				 return (Table) a;
	return null;
 }
 
 /**
  *  Used to extract the PrimaryKey annotation from a Class
  * @param tab Classs which maps to a table with a primary key
  * @return PrimaryKey annotation
  */
 public PrimaryKey getPrimaryKey(Class<?> tab) {
	 for(Field f:tab.getFields())
	 for(Annotation a:f.getAnnotations())
		 if(a instanceof PrimaryKey) 
			 return (PrimaryKey) a;
	 return null;
}
 
 /**
  *  Method used to insert an object into the database
  * @param o Object to inser
  * @return void
  */
 public int insert(Object o) {
       	
	 TableData td=this.convertTable(o.getClass(),this.getTable(o.getClass()));
	 TableHierarchyValue hierarchy = null;
	 TableHierarchyData thd=this.structuretablesuper(td.table, o.getClass());
	 hierarchy=this.convertTableHierarchyValue(thd,o);
	 PrimaryKey last_pk=null;
	 Object last_val=null;
	 if(hierarchy.hierarchy!=null) {
	       for(int i=hierarchy.hierarchy.size()-1;i>=0;i--) {
		     TableValue tv=hierarchy.hierarchy.get(i);     
		     last_val=this.dbc.insert(o,tv,last_pk,last_val);
		     last_pk=hierarchy.hierarchy.get(i).pk;
	       }
	 }
	 return this.dbc.insert(o,hierarchy.current,last_pk,last_val);
 }

 
}
