package orm;

import java.lang.reflect.Field;


import annotations.PrimaryKey;
import annotations.Table;

public abstract class TableInfo {
	public final Table table;
	public final PrimaryKey pk;	
	public final Field pk_field;
	public final Class<?> class_name;
	public TableInfo(Table table,PrimaryKey pk,Field pk_field,Class<?> class_name) {
		this.table=table;
		this.pk=pk;
		this.pk_field=pk_field;
		this.class_name=class_name;
	}
}
