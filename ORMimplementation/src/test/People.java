package test;

import java.util.List;

import annotations.Column;
import annotations.OneToMany;
import annotations.PrimaryKey;
import annotations.Table;

@Table(name = "People")
public class People {	
	@Column(name="Name")
	public String name;
	
	@OneToMany()
	public List<Car> c;
	
	@PrimaryKey(name="pid",autoincrement=true)
	public int pid;
	
	public People() {
		
	}
	
	public People(List<Car> c,String name) {
		this.name = name;
		this.c=c;		
	}
	
}
