package orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import annotations.Column;
import annotations.OneToMany;
import annotations.OneToOne;
import annotations.PrimaryKey;
import annotations.Table;
import connector.Criteria;
import connector.CriteriaSet;
import connector.DatabaseConnector;
import orm.TableData;
import exception.CommunicationException;
import exception.DbDriverNotFound;


public class ORMLoader {
 private final DatabaseConnector dbc;
 
 public ORMLoader(DatabaseConnector dbc) {
	 this.dbc=dbc;
 }
 
 public List<Object> get(Class<?> class_name,String field,String match) throws DbDriverNotFound, CommunicationException{
	 List<TableData> hierarchy;
	for(Annotation a:class_name.getAnnotations()) {
		if(a instanceof Table && !dbc.checkTable(((Table) a).name())) {
			hierarchy=this.structuretablesuper((Table) a, class_name);
		}
	}
	return null;
	 
 }
 
 
 
 private List<TableData>  structuretablesuper(Table t,Class<?> class_name) {
	 Table table_super=null,taux=t;
	 PrimaryKey primary_super=null,primary_current=null;
	 Class<?> current_class=class_name;
	 TableData tab_sup_data=null,tab_cur_data=null;
	 List<TableData> hierarchy=new ArrayList<TableData> ();
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
				 this.structuretablesuper(this.getTable((Class<?>) foreign_oneto), (Class<?>) foreign_oneto);
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
	return hierarchy;
 }

 
 private void createTable(TableData current_table,TableData super_table) {
	 //System.out.println(table_current+" "+key_current+" "+foreign_table+" "+foreign_key);
	 String table_current_name=current_table==null?"":current_table.table.name();
	 String key_current_name=current_table==null?"":current_table.pk==null?"":current_table.pk.name();
	 String foreign_table_name=super_table==null?"":super_table.table.name();
	 String foreign_key_name=super_table==null?"":super_table.pk==null?"":super_table.pk.name();
	 dbc.createTable(current_table,super_table);
	 System.out.println("Create table "+table_current_name+" primary key: "+key_current_name+" with foreign table: "+foreign_table_name+" foreign key: "+foreign_key_name);
 }
 
 private void updateTableForeignKey(TableData current,TableData foreign) {
	 dbc.updateTableForeignKey(current,foreign);
 }
 
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
 
 public TableValue convertTableValue(TableData td,Object o){
	 if(!td.table.name().equals(this.getTable(o.getClass()).name()))
		 return null;
	 List<ColumnValue> lcv=new ArrayList<ColumnValue>();
	 Object pk_val=null;
	try {
		pk_val = td.pk_field.get(o);
	} catch (IllegalArgumentException | IllegalAccessException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	 for(ColumnData cd:td.lcd)
		if(td.table.name().equals(this.getTable(o.getClass()).name())) {
			Object col_val=null;
			try {
				col_val=cd.f.get(o);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();}
			lcv.add(new ColumnValue(cd,col_val));	
		}
	return new TableValue(lcv,td,pk_val);
	 
 }
 
 public CriteriaSet setCriteria(Class<?> table) {
	 TableData td=this.convertTable(table, this.getTable(table));
	 List<TableData> hierarchy=this.structuretablesuper(td.table, table);
	 return dbc.setCriteria(td,hierarchy);
 }
 
 public Table getTable(Class<?> tab) {
		 for(Annotation a:tab.getAnnotations())
			 if(a instanceof Table) 
				 return (Table) a;
	return null;
 }
 
 public PrimaryKey getPrimaryKey(Class<?> tab) {
	 for(Field f:tab.getFields())
	 for(Annotation a:f.getAnnotations())
		 if(a instanceof PrimaryKey) 
			 return (PrimaryKey) a;
return null;
}
 
 
 public boolean insert(Object o) {
	 TableData td=this.convertTable(o.getClass(),this.getTable(o.getClass()));
	 List<TableData> hierarchy;
	 try {
		if(!dbc.checkTable(td.table.name()))
			hierarchy=this.structuretablesuper(td.table, o.getClass());
	} catch (DbDriverNotFound | CommunicationException e) {
		e.printStackTrace();}
	 return this.dbc.insert(o,this.convertTableValue(td, o));
 }

 
}
