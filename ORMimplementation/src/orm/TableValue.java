package orm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import annotations.PrimaryKey;
import annotations.Table;

public class TableValue extends TableInfo {
	public final List<ColumnValue> lcv;
	public final Object pk_val,value;
	List<TableValue> foreign_val_key=null;
	public TableValue(List<ColumnValue> lcv, Table table, PrimaryKey pk,Field pk_field,Class<?> class_name,Object pk_val,Object value) {
		super(table, pk, pk_field, class_name);
		this.lcv=lcv;
		this.pk_val=pk_val;
		this.value=value;
	}
	
	public TableValue(List<ColumnValue> lcv,TableData td,Object pk_val,Object value) {
		super(td.table, td.pk, td.pk_field, td.class_name);
		this.lcv=lcv;
		this.pk_val=pk_val;
		this.value=value;
	}

	public boolean addForeignComposition(TableValue tab) {
		if(tab instanceof TableValue) {
			TableValue tv=(TableValue) tab;
			if(foreign_val_key==null)
				foreign_val_key=new ArrayList<TableValue>();
			this.foreign_val_key.add(tv);
			return true;
		}	
		return false;
	}

}
