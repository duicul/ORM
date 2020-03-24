package test;

import java.util.List;

import annotations.*;

@Table(name="Student")
public class Student extends People {

	@PrimaryKey(name="sid")
	public final int sid;
	
	@Column(name="grade")
	public final int grade;
	
	public Student(int pid,int sid, List<Car> c,int grade) {
		super(pid, c);
		this.sid = sid;
		this.grade=grade;
	}
}
