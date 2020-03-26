package test;

import java.util.List;

import annotations.Column;
import annotations.PrimaryKey;
import annotations.Table;

@Table(name="StudentLiterature")
public class StudentLiterature extends Student {
	
	@Column(name="specialization")
	public String spec;
	
	@PrimaryKey(name="slid")
	public int slid;
	
	public StudentLiterature() {
		
	}
	
	public StudentLiterature(List<Car> c, int grade,String spec,String name) {
		super(c, grade,name);
		this.spec = spec;
	}


	

}
