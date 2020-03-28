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
import connector.Pair;

public abstract class ORMConverter {

      /**
       * Used to add the values into a TableData structure
       * @param td TableData structure containing table metadata mapped to a class
       * @param o Value to be added
       * @return new structure containing the metadata and the value for each field/column
       */
      public static TableValue convertTableValue(TableData td,Object o){
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
     	return new TableValue(lcv,td,pk_val,o);
     	 
      }
      
      /**
       * Used to add the values to a tree metadata structure for the <b>hierarchical</b> structure of a class
       * @param td Metadata tree structure containing the objects structure
       * @param o Value to be added
       * @return TableHierarchyValue containing the value added mapped to the metadata structure
       */
      private static TableHierarchyValue convertTableHierarchyValueClass(TableHierarchyData td,Object o){
     	 List<TableValue> ltv_fore=null;
     	 TableValue curr_value=convertTableValue(td.current,o);
     	 for(TableData tdi:td.hierarchy) {		 
     		 if(ltv_fore==null)
     			 ltv_fore=new ArrayList<TableValue>();
     		 TableValue tv_curr=convertTableValue(tdi,o);
     		 if(tv_curr!=null)
     			 ltv_fore.add(tv_curr);
     	 }
     	 return new TableHierarchyValue(ltv_fore,curr_value);
      }
      
      /**
       * Used to add the values to a tree metadata structure containing both hierarchical and compositional structure of a class
       * @param td Metadata tree structure containing the objects structure
       * @param o Value to be added
       * @return TableHierarchyValue containing the value added mapped to the metadata structure
       */ 
      public static TableHierarchyValue convertTableHierarchyValue(TableHierarchyData td,Object o) {
            TableHierarchyValue thv=convertTableHierarchyValueClass(td, o);
            if(thv!=null) {
     	     thv.addForeignHierarchy(extractOneToValue(td.current,td.foreign_hie, o));
     	     for(TableData tdi:td.hierarchy)
     		   thv.addForeignHierarchy(extractOneToValue(tdi,td.foreign_hie, o));}
            return thv;
      }
      
      /**
       *  Extract the metadata for a given table
       * @param table for which the metadata is extracted
       * @param t class mapped to the table
       * @return metadata structure TableData
       */
      public static TableData convertTable(Class<?> table){
	 Table t=getTable(table);
     	 List<ColumnData> lcd=new ArrayList<ColumnData>();
     	 PrimaryKey pk=null;
     	 Field pk_field=null;
     	 List<TableData> foreign_keys=new ArrayList<TableData>();
     	 for(Field f:table.getDeclaredFields()) {
     		 Column c=null;
     		 OneToMany otm=null;
     		 OneToOne oto=null;
     		 for(Annotation a:f.getAnnotations()) {
     			 if(a instanceof Column) {
     				 c=(Column) a;
     				 continue;}
     		 	 if(a instanceof OneToMany) {
     		 		 otm=(OneToMany) a;
     		 		Type list_oneto=f.getGenericType();
     				 if(list_oneto instanceof ParameterizedType) {
     				       ParameterizedType paramtype=(ParameterizedType) list_oneto;
     				       if(((ParameterizedType) list_oneto).getActualTypeArguments().length>0)
     					foreign_keys.add(ORMConverter.convertTable((Class<?>) paramtype.getActualTypeArguments()[0]));
     				 }  		 		
     		 		 continue;}
     		 	 if(a instanceof OneToOne) {
     		 		 oto=(OneToOne) a;
     		 		foreign_keys.add(ORMConverter.convertTable((Class<?>) f.getGenericType()));     		 		 
     		 		 continue;}
     		 	if(a instanceof PrimaryKey) {
     		 		 pk_field=f;
     		 		 pk=(PrimaryKey) a;
     		 		 continue;}
     		 }
     		 lcd.add(new ColumnData(c, otm, oto,f));
     	 }	 
     	 return new TableData(lcd,t,pk, pk_field, table,foreign_keys);
      }
      
      /**
       * Used to extract the foreign tables TableHierarchyValue lists from an object based on a TableHierarchyData list describing the foreign tables
       * @param primary main table's TableData structure
       * @param foreign table TableHierarchyData list describing their structure
       * @param o object from which to extract the table values
       * @return List of the corresponding table's metadata + values TableHierarchyValue structure
       */
      @SuppressWarnings("unchecked")
     private static List<TableHierarchyValue> extractOneToValue(TableData primary,List<TableHierarchyData> foreign,Object o) {
     	 List<ColumnData> onetomany=new ArrayList<ColumnData>(),onetoone=new ArrayList<ColumnData>();
     	 //List<TableData> rettabd=new ArrayList<TableData>();
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
     					  TableHierarchyValue thv=convertTableHierarchyValue(thd, obj);
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
     					    TableHierarchyValue thv=convertTableHierarchyValue(thd, fore_val);
     					    if(thv!=null)
     						  ot_hie_val.add(thv);
     				      }
     			 }
     		 }
     	 }
     	 return ot_hie_val.size()==0?null:ot_hie_val; 
      }
      
      
      /**
       *  Used to extract the Table annotation from a Class
       * @param tab Classs which maps to a table 
       * @return Table annotation
       */
      
      public static Table getTable(Class<?> tab) {
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
      public static PrimaryKey getPrimaryKey(Class<?> tab) {
     	 for(Field f:tab.getFields())
     	 for(Annotation a:f.getAnnotations())
     		 if(a instanceof PrimaryKey) 
     			 return (PrimaryKey) a;
     	 return null;
     }
      
      /**
       *  Extract table higher hierarchy including the current table
       * @param table class for mapped to the table for which the hierarchy is extracted
       * @return hierarchy list
       */
      public static List<TableData> extractFullTableHierarchy(Class<?> table){
	      Class<?> curr=table;
	      TableData t=ORMConverter.convertTable(curr);
	      List<TableData> hierar=null;
	      while(t!=null) {
		    if(hierar==null)
			  hierar=new LinkedList<TableData>();
		    hierar.add(t);
		    curr=curr.getSuperclass();
		    if(curr.equals(Object.class))
			  break;
		    t=ORMConverter.convertTable(curr);
		    }
	      return hierar;
	}
      
      public static List<Table> getRelatingTables(TableHierarchyData thd){
	    	List<Table> tabls=new LinkedList<Table>();
	    	tabls.add(thd.current.table);
	    	for(TableData tdi:thd.hierarchy)
	    	      tabls.add(tdi.table);
	    	if(thd.foreign_hie!=null)
	    	      for(TableHierarchyData thdi:thd.foreign_hie)
	    		    tabls.addAll(getRelatingTables(thdi));
	    
	    return tabls;
      }
      
      public static TableHierarchyData  extractHierarchicalData(Class<?> class_name) {
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
				       foreign_table.add(extractHierarchicalData( (Class<?>) foreign_oneto));
					 //System.out.println((Class<?>) foreign_oneto);
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
	 }
      
      public static TableData getForeignTableMainFromFullHierarchy(List<TableData> hierarchy,TableData foreign) {
	    for(TableData tdhie:hierarchy)
		  if(tdhie.foreign_val_key!=null)
		  for(TableData tdifor:tdhie.foreign_val_key)
			  if(foreign.table.name().equals(tdifor.table.name()))
				return tdhie;
	    return null;
      }
      
      public static TableData getForeignTableMainFromFullHierarchy(TableData current,List<TableData> hierarchy,TableData foreign) {
	    if(current.foreign_val_key!=null)
	    for(TableData tdifor:current.foreign_val_key)
		  if(foreign.table.name().equals(tdifor.table.name()))
			return current;
	    return getForeignTableMainFromFullHierarchy(hierarchy,foreign);
      }
      
      public static Class<?> extractOneToReference(ColumnData cd){
	    	   Type foreign_oneto=null;
		   if(cd.oto!=null&&cd.f!=null) {
			 foreign_oneto=cd.f.getGenericType();
			 return (Class<?>) foreign_oneto;}
		   if(cd.otm!=null) {
			 Type list_oneto=cd.f.getGenericType();
			 if(list_oneto instanceof ParameterizedType) {
			       ParameterizedType paramtype=(ParameterizedType) list_oneto;
			       if(((ParameterizedType) list_oneto).getActualTypeArguments().length>0)
				     foreign_oneto=paramtype.getActualTypeArguments()[0];
			 }
			 return (Class<?>) foreign_oneto;
		   }
		  
	    
	    return null;
	    
      }
      
      /**
       * Obtain the primary key and its value from the main table for a class representing the foreign table
       * @param insert_ids_hie list of tables from which to match the class_name/foreign table value and primary key
       * @param hierarchy of tables of the main table primary keys and their inserted values from which to extract the foreign table by matching the field by class
       * @param ColumnClass representing the foreign table
       * @return both the PrimaryKey annotation and it's specific value
       */
      public static Pair<PrimaryKey,Object> getForeignKeyForColumn(List<Pair<Class<?>,Object>> insert_ids_hie,List<TableData> hierarchy,Class<?> ColumnClass){
            PrimaryKey corr_fore_pk=null;
            Object foreign_id_val=null;
            for(TableData tdi:hierarchy)
     	     for(ColumnData cd:tdi.lcd) {
     		   Type foreign_oneto=null;
     		   if(cd.oto!=null&&cd.f!=null)
     			 foreign_oneto=cd.f.getGenericType();
     		   if(cd.otm!=null) {
     			 Type list_oneto=cd.f.getGenericType();
     			 if(list_oneto instanceof ParameterizedType) {
     			       ParameterizedType paramtype=(ParameterizedType) list_oneto;
     			       if(((ParameterizedType) list_oneto).getActualTypeArguments().length>0)
     				     foreign_oneto=paramtype.getActualTypeArguments()[0];
     			 }
     		   }
     		   if(foreign_oneto!=null&&foreign_oneto.equals(ColumnClass)) {
     			 for(Pair<Class<?>,Object> p:insert_ids_hie)
     			       if(tdi.class_name.equals(p.l))
     				     foreign_id_val=p.r;
     			 corr_fore_pk=tdi.pk;
     			 break;}

     	     }
            if(corr_fore_pk!=null) 
     	     return new Pair<PrimaryKey,Object>(corr_fore_pk,foreign_id_val);
           return null;
      }
}
