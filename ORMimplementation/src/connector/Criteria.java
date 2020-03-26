package connector;

import java.util.ArrayList;
import java.util.List;

import annotations.Table;
import exception.CommunicationException;
import exception.ConstructorException;
import exception.DbDriverNotFound;
import exception.NoSuchColumnException;
import orm.ColumnData;
import orm.TableData;

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
