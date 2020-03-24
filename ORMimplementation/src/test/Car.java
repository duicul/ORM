package test;

import annotations.Column;
import annotations.PrimaryKey;
import annotations.Table;

@Table(name = "Car")
public class Car {
	@Column(name="Model")
	public final String model;
	@Column(name="Color")
	public final String color;
	@Column(name="RegistrationNumber")
	public final String reg_no;
	
	@PrimaryKey(name="cid")
	public final int cid;
	
	public Car(String model,String color,String reg_no,int cid) {
		this.color=color;
		this.model=model;
		this.reg_no=reg_no;
		this.cid=cid;
	}
	
}
