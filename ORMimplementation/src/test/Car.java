package test;

import annotations.Column;
import annotations.PrimaryKey;
import annotations.Table;

@Table(name = "Car")
public class Car {
	@Column(name="Model")
	public String model;
	@Column(name="Color")
	public String color;
	@Column(name="RegistrationNumber")
	public String reg_no;
	@PrimaryKey(name="cid")
	public int cid;
	
	public Car() {
		
	}
	
	public Car(String model,String color,String reg_no) {
		this.color=color;
		this.model=model;
		this.reg_no=reg_no;
	}
	
}
