package connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import annotations.PrimaryKey;
import annotations.Table;
import exception.CommunicationException;
import exception.DbDriverNotFound;
import orm.ColumnData;
import orm.TableData;

public class MariaDBConnector extends DatabaseConnector {
	public final String driver;
	public MariaDBConnector(long port, String hostname, String username, String password, String database) {
		super(port, hostname, username, password, database);
		this.driver="org.mariadb.jdbc.Driver";
	}

	@Override
	public boolean checkTable(String table) throws DbDriverNotFound, CommunicationException {
		try {
			Class.forName(this.driver);
			Connection con=DriverManager.getConnection(  
					"jdbc:mariadb://"+this.hostname+":"+this.port+"/"+this.database,this.username,this.password);
			ResultSet rs=con.getMetaData().getTables(null,null,table,null);
			con.close();
			return rs.next();
		} catch (ClassNotFoundException e) { 
			throw new DbDriverNotFound();}
		catch (SQLException e) {
			throw new CommunicationException();}
	}

	@Override
	public boolean createTable(TableData current,TableData foreign) {
		try{  
			Class.forName(this.driver);  
			Connection con=DriverManager.getConnection(  
			"jdbc:mysql://127.0.0.1:3306/"+database,username,password);   
			Statement stmt=con.createStatement(); 
			String sql = "CREATE TABLE " + current.table.name();
			sql+="(";
			for(ColumnData cd:current.lcd) {
				String coltype=null;
				if(cd.t.equals(int.class)||cd.t.equals(Integer.class))
					coltype="INTEGER";
				if(cd.t.equals(String.class))
					coltype="VARCHAR(255)";
				if(cd.t.equals(char.class))
					coltype="VARCHAR(1)";
				if(coltype!=null)
					if(cd.col!=null)
						sql+=cd.col.name()+" "+coltype+" , ";
					else if(cd.pk!=null)
						sql+=cd.pk.name()+" "+coltype+" , ";
					else if(cd.otm!=null)
						sql+=cd.otm.column()+" "+coltype+" , ";
					else if(cd.oto!=null)
						sql+=cd.oto.column()+" "+coltype+" , ";
			}
			
			if(current.pk!=null)
				sql+=" PRIMARY KEY ( "+current.pk.name()+" )";
			sql+=");";
	      stmt.executeUpdate(sql);
	      con.close();   
	      return true;
			}catch(Exception e)
		{ e.printStackTrace();
		return false;}
	}

	@Override
	public Object get(Class<?> dao, String Column, String condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(Class<?> dao, Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Criteria setCriteria(Class<?> table) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateTableForeignKey(TableData current,TableData foreign) {
		// TODO Auto-generated method stub
		return false;
	}

}
