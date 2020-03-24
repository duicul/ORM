package orm;

import java.lang.reflect.Type;

import annotations.*;

public class ColumnData {
	public final Column col;
	public final OneToMany otm;
	public final OneToOne oto;
	public final Type t;
	public final PrimaryKey pk;
	
	public ColumnData(Column col,OneToMany otm,OneToOne oto,Type t,PrimaryKey pk) {
		this.col=col;
		this.otm=otm;
		this.oto=oto;
		this.t = t;
		this.pk=pk;
	}
}
