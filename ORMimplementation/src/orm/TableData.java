package orm;

import java.util.List;

import annotations.PrimaryKey;
import annotations.Table;

public class TableData {
	public final List<ColumnData> lcd;
	public final Table table;
	public final PrimaryKey pk;
	
	public TableData(List<ColumnData> lcd,Table table,PrimaryKey pk) {
		this.lcd=lcd;
		this.table=table;
		this.pk=pk;
	}
}
