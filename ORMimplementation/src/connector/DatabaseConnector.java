package connector;

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

public abstract boolean checktable(String table);

}
