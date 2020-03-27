package connector;

import annotations.Table;


public class Criteria {
	private final Table t;
	private final String rest;
	public Criteria(Table t,String rest) {
		this.t=t;
		this.rest=rest;
	}
	
	public String getCriteria() {
		return this.t.name()+this.rest;
	}
	
	public Table getCriteriaTable() {
		return this.t;
	}
	
}
