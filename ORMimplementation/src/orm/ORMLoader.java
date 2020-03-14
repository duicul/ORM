package orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;


import annotations.PrimaryKey;
import annotations.Table;


public class ORMLoader {
 public List<Object> get(Class<?> class_name,String field,String match){
	for(Annotation a:class_name.getAnnotations()) {
		if(a instanceof Table)
			this.structuretable((Table) a, class_name);
		//System.out.println(a.toString());
		//System.out.println(a.annotationType());
	}
	return null;
	 
 }
 
 
 
 public void  structuretable(Table t,Class<?> class_name) {
	 Table table_super=null;
	 PrimaryKey primary_super=null,primary_current=null;
	 if(!class_name.equals(Object.class)) {
	 
	 for(Annotation a:class_name.getSuperclass().getAnnotations())
		 if(a instanceof Table) {
			 table_super=(Table)a;
			 break;}
	 if(table_super!=null) {
		 for(Field f:class_name.getSuperclass().getDeclaredFields()){
			 //System.out.println(f);
		 for(Annotation a:f.getAnnotations())
			 if(a instanceof PrimaryKey)
				 primary_super=(PrimaryKey)a;
		 }
	 }
	 }
	 for(Field f:class_name.getDeclaredFields()) {
		 //System.out.println(f);
		 for(Annotation a:f.getAnnotations())
			 if(a instanceof PrimaryKey)
				 primary_current=(PrimaryKey)a;}
	 this.createtable(t,primary_current,table_super,primary_super);
 }

 
 public void createtable(Table table_current,PrimaryKey key_current,Table foreign_table,PrimaryKey foreign_key) {
	 //System.out.println(table_current+" "+key_current+" "+foreign_table+" "+foreign_key);
	 String table_current_name=table_current==null?"":table_current.name();
	 String key_current_name=key_current==null?"":key_current.name();
	 String foreign_table_name=foreign_table==null?"":foreign_table.name();
	 String foreign_key_name=foreign_key==null?"":foreign_key.name();
	 System.out.println("Create table "+table_current_name+" primary key: "+key_current_name+" with foreign table: "+foreign_table_name+" foreign key: "+foreign_key_name);
 }

}
