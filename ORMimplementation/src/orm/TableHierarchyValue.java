package orm;

import java.util.List;

public class TableHierarchyValue extends TableHierarchy {
	public TableValue current=null;
	public List<TableValue> hierarchy=null;
	public List<TableHierarchyValue> foreign_hie=null;
	public TableHierarchyValue(List<TableValue> hierarchy,TableValue current,List<TableHierarchyValue> foreign_hie) {
		this.current=current;
		this.hierarchy=hierarchy;
		this.foreign_hie=foreign_hie;
	}

}
