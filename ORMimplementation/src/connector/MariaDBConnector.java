package connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MariaDBConnector extends DatabaseConnector {
	public final String driver;
	public MariaDBConnector(long port, String hostname, String username, String password, String database) {
		super(port, hostname, username, password, database);
		this.driver="org.mariadb.jdbc.Driver";
	}

	@Override
	public boolean checktable(String table) {
		try {
			Class.forName(this.driver);
			Connection con=DriverManager.getConnection(  
					"jdbc:mariadb://"+this.hostname+":"+this.port+"/"+this.database,this.username,this.password);
			ResultSet rs=con.getMetaData().getTables(null,null,table,null);
			con.close();
			return rs.next();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return false;
		} 
	}

}
