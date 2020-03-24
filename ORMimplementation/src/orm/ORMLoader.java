package orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import annotations.Column;
import annotations.OneToMany;
import annotations.OneToOne;
import annotations.PrimaryKey;
import annotations.Table;
import connector.Criteria;
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
	
	for(Annotation a:class_name.getAnnotations()) {
		if(a instanceof Table && !dbc.checkTable(((Table) a).name())) {
			this.structuretablesuper((Table) a, class_name);
		}
		//System.out.println(a.toString());
		//System.out.println(a.annotationType());
	}
	return null;
	 
 }
 
 
 
 private void  structuretablesuper(Table t,Class<?> class_name) {
	 Table table_super=null;
	 PrimaryKey primary_super=null,primary_current=null;
	 TableData tab_sup_data=null,tab_cur_data=null;
	 
	 if(!class_name.equals(Object.class)) {
		 Class<?> super_cls=class_name.getSuperclass();
		 for(Annotation a:super_cls.getAnnotations())
			 if(a instanceof Table) {
				 table_super=(Table)a;
				 break;}
		 if(table_super!=null) {
			 this.structuretablesuper(table_super, super_cls);
			 for(Field f:class_name.getSuperclass().getDeclaredFields()){
				 //System.out.println(f);
				 for(Annotation a:f.getAnnotations())
					 if(a instanceof PrimaryKey) {
						 primary_super=(PrimaryKey)a;
				 		 tab_sup_data=this.convertTable(super_cls,table_super,primary_super);}
			 }
		 }
	 }
	 for(Field f:class_name.getDeclaredFields()) {
		 //System.out.println(f);
		 for(Annotation a:f.getAnnotations())
			 if(a instanceof PrimaryKey) {
				 primary_current=(PrimaryKey)a;
				 tab_cur_data=this.convertTable(class_name,t,primary_current);
			 }
	 }
	 try {
		if(table_super!=null&&dbc.checkTable(table_super.name()))
			this.updateTableForeignKey(tab_cur_data,tab_sup_data);
		 this.createTable(tab_cur_data,tab_sup_data);
	} catch (DbDriverNotFound | CommunicationException e) {
		e.printStackTrace();
	}
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
 
 public TableData convertTable(Class<?> table,Table t,PrimaryKey pk){
	 List<ColumnData> lcd=new ArrayList<ColumnData>();
	 for(Field f:table.getDeclaredFields()) {
		 Type type=null;
		 Column c=null;
		 OneToMany otm=null;
		 OneToOne oto=null;
		 type=f.getGenericType();
		 boolean primary=false;
		 for(Annotation a:f.getAnnotations()) {
			 if(a instanceof Column)
				 c=(Column) a;
		 	 if(a instanceof OneToMany)
		 		 otm=(OneToMany) a;
		 	 if(a instanceof OneToOne)
		 		 oto=(OneToOne) a;
		 	if(a instanceof PrimaryKey)
		 		primary=true;
		 }
		 lcd.add(new ColumnData(c, otm, oto, type,primary==true?pk:null));
	 }
	 
	 
	 return new TableData(lcd,t,pk);
 }
 
 public Criteria setCriteria(Class<?> table) {
	 return dbc.setCriteria(table);
 }
 
}
