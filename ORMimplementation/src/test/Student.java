package test;

import java.util.List;

import annotations.*;

@Table(name="Student")
public class Student extends People {	
	@Column(name="grade")
	public int grade;
	
	@PrimaryKey(name="sid")
	public int sid;
	
	public Student() {
		
	}
	
	public Student(List<Car> c,int grade,String name) {
		super(c,name);
		this.grade=grade;
	}
	
	
}
