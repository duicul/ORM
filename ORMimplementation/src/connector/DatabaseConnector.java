package connector;

import java.util.List;

import annotations.PrimaryKey;
import annotations.Table;
import exception.CommunicationException;
import exception.DbDriverNotFound;
import orm.ColumnData;
import orm.TableData;

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
	
	public abstract Criteria setCriteria(Class<?> table);
	public abstract boolean checkTable(String table_name) throws DbDriverNotFound, CommunicationException;
	public abstract boolean createTable(TableData current,TableData foreign);
	public abstract boolean updateTableForeignKey(TableData current,TableData foreign);
	public abstract Object get(Class<?> dao,String Column,String condition);
	public abstract boolean add(Class<?> dao,Object o);
	
	
}
