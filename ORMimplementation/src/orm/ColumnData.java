package orm;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import annotations.*;

public class ColumnData {
	public final Column col;
	public final OneToMany otm;
	public final OneToOne oto;
	public final Field f;
	
	public ColumnData(Column col,OneToMany otm,OneToOne oto,Field f) {
		this.col=col;
		this.otm=otm;
		this.oto=oto;
		this.f=f;
	}
}
