package orm;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import annotations.Column;
import annotations.OneToMany;
import annotations.OneToOne;
import annotations.PrimaryKey;

public class ColumnValue extends ColumnData {
	public final Object value;
	public ColumnValue(Column col, OneToMany otm, OneToOne oto,Field f,Object value) {
		super(col, otm, oto,f);
		this.value = value;
	}
	
	public ColumnValue(ColumnData cd,Object value) {
		super(cd.col,cd.otm,cd.oto,cd.f);
		this.value=value;
	}
}
