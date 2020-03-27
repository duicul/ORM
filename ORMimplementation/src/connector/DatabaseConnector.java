package connector;

import java.lang.reflect.Type;
import java.util.List;

import annotations.PrimaryKey;
import exception.CommunicationException;
import exception.ConstructorException;
import exception.DbDriverNotFound;
import orm.TableData;
import orm.TableHierarchyData;
import orm.TableHierarchyValue;
import orm.TableValue;

public abstract class DatabaseConnector {
	public final long port;
	public final String hostname,username,password,database;

	public DatabaseConnector(long port,String hostname,String username,String password,String database) {
		this.port=port;
		this.hostname=hostname;
		this.username=username;
		this.password=password;
		this.database=database;
	}
	
	public static String getDataBaseType(Type type) {
		if(type.equals(int.class)||type.equals(Integer.class))
			return "INTEGER";
		if(type.equals(float.class)||type.equals(Float.class))
			return "FLOAT";
		if(type.equals(double.class)||type.equals(Double.class))
			return "DOUBLE";
		if(type.equals(String.class))
			return "VARCHAR(255)";
		if(type.equals(char.class))
			return "VARCHAR(1)";
		return null;
	}
	
	public abstract CriteriaSet setCriteria(TableData table,TableHierarchyData hierarchy);
	public abstract boolean checkTable(String table_name) throws DbDriverNotFound, CommunicationException;
	public abstract boolean createTable(TableData current,TableData foreign);
	public abstract boolean updateTableForeignKey(TableData current,TableData foreign);
	public abstract List<Object> select(CriteriaSet cs) throws ConstructorException, DbDriverNotFound, SecurityException, CommunicationException;
	/**
	 * 
	 * @param o
	 * @param table
	 * @param foreign
	 * @param foreign_val
	 * @param foreignpks
	 * @return Insert id
	 */
	public abstract int insert(Object o, TableValue table, PrimaryKey foreign,Object foreign_val,Pair<PrimaryKey,Object> foreignpks);
	
	public abstract boolean dropTable(TableData t);
	public abstract boolean cleanTable(TableData t);
}
