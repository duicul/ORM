package test;

import annotations.*;

@Table(name="Student")
public class Student extends People {
	@PrimaryKey(name="sid")
	int sid;
}
