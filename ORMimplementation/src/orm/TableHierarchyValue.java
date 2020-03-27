package orm;

import java.util.LinkedList;
import java.util.List;

public class TableHierarchyValue extends TableHierarchy {
	public TableValue current=null;
	public List<TableValue> hierarchy=null;
	public List<List<TableHierarchyValue>> foreign_hie=null;
	public TableHierarchyValue(List<TableValue> hierarchy,TableValue current) {
		this.current=current;
		this.hierarchy=hierarchy;
	}
	
	public void addForeignHierarchy(List<TableHierarchyValue> thv) {
		if(foreign_hie==null)
			this.foreign_hie=new LinkedList<List<TableHierarchyValue>>();
		if(thv!=null)
		      this.foreign_hie.add(thv);
	}
	
	/*public TableHierarchyValue(TableHierarchyData thd,TableValue current) {
		this.current=current;
		List<TableHierarchyValue> lthv=new ArrayList<TableHierarchyValue>();
		for(TableHierarchyData thdi:thd.foreign_hie)
			lthv.add(new )
		this.foreign_hie=new TableValue(thd.foreign_hie);
		this.hierarchy=thd.hierarchy;
	}*/

}
