package test;

import java.util.List;

import annotations.Column;
import annotations.PrimaryKey;
import annotations.Table;

@Table(name="StudentLiterature")
public class StudentLiterature extends Student {
	@PrimaryKey(name="slid")
	public final int slid;
	
	@Column(name="specialization")
	public final String spec;
	
	public StudentLiterature(int pid,int sid,int slid, List<Car> c, int grade,String spec) {
		super(pid,sid, c, grade);
		this.slid = slid;
		this.spec = spec;
	}


	

}
