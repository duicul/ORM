package orm;

import java.util.List;

public class TableHierarchyData extends TableHierarchy {
	public TableData current=null;
	public List<TableData> hierarchy=null;
	public List<TableHierarchyData> foreign_hie=null;
	public TableHierarchyData(List<TableData> hierarchy,TableData current,List<TableHierarchyData> foreign_hie) {
		this.current=current;
		this.hierarchy=hierarchy;
		this.foreign_hie=foreign_hie;
	}

}
