package orm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import annotations.PrimaryKey;
import annotations.Table;

public class TableData extends TableInfo {
	public final List<ColumnData> lcd;
	List<TableData> foreign_val_key=null;
	public TableData(List<ColumnData> lcd, Table table, PrimaryKey pk,Field pk_field,Class<?> class_name) {
		super(table, pk,pk_field, class_name);
		this.lcd=lcd;
	}
	@Override
	public boolean addForeignComposition(TableInfo tab) {
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
