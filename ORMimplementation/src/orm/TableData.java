package orm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import annotations.PrimaryKey;
import annotations.Table;

public class TableData extends TableInfo {
	public final List<ColumnData> lcd;
	List<TableData> foreign_val_key=null;
	
	public TableData(List<ColumnData> lcd, Table table, PrimaryKey pk,Field pk_field,Class<?> class_name,List<TableData> foreign_val_key) {
		super(table, pk,pk_field, class_name);
		this.lcd=lcd;
		this.foreign_val_key=foreign_val_key;
	}
	
	public boolean addForeignComposition(TableData tab) {
		if(tab instanceof TableData) {
			TableData tv=(TableData) tab;
			if(foreign_val_key==null)
				foreign_val_key=new ArrayList<TableData>();
			this.foreign_val_key.add(tv);
			return true;
		}	
		return false;
	}

}
