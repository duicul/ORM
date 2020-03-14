package test;

import annotations.PrimaryKey;
import annotations.Table;

@Table(name = "People")
public class People {
	@PrimaryKey(name="pid")
	int pid;
}
