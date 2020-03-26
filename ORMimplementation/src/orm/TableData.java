package orm;

import java.lang.reflect.Field;
import java.util.List;

import annotations.PrimaryKey;
import annotations.Table;

public class TableData extends TableInfo {
	public final List<ColumnData> lcd;
	public TableData(List<ColumnData> lcd, Table table, PrimaryKey pk,Field pk_field,Class<?> class_name) {
		super(table, pk,pk_field, class_name);
		this.lcd=lcd;
	}

}
