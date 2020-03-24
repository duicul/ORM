package test;

import java.util.List;

import annotations.OneToMany;
import annotations.PrimaryKey;
import annotations.Table;

@Table(name = "People")
public class People {
	@PrimaryKey(name="pid")
	public final int pid;
	
	@OneToMany(column="cid")
	public final List<Car> c;
	
	public People(int pid,List<Car> c) {
		this.pid=pid;
		this.c=c;		
	}
	
}
