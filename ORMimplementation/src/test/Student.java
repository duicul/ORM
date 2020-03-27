package test;

import java.util.List;

import annotations.*;

@Table(name="Student")
public class Student extends People {	
	@Column(name="grade")
	public int grade;
	
	@OneToOne()
	public Nota n;
	
	@PrimaryKey(name="sid")
	public int sid;
	
	public Student() {
		
	}
	
	public Student(List<Car> c,int grade,String name,Nota n) {
		super(c,name);
		this.grade=grade;
		this.n=n;
	}
	
	
}
