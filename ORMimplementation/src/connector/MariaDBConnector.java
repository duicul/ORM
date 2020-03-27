package connector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import annotations.PrimaryKey;
import exception.AutoIncrementValueException;
import exception.AutoIncrementWrongTypeException;
import exception.CommunicationException;
import exception.ConstructorException;
import exception.DbDriverNotFound;
import orm.ColumnData;
import orm.ColumnValue;
import orm.TableData;
import orm.TableHierarchyData;
import orm.TableHierarchyValue;
import orm.TableValue;

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
				String coltype=DatabaseConnector.getDataBaseType(cd.f.getGenericType());
				if(coltype!=null)
					if(cd.col!=null)
						sql+=cd.col.name()+" "+coltype+" , ";
					/*else if(cd.otm!=null)
						sql+=cd.otm.column()+" "+coltype+" , ";
					else if(cd.oto!=null)
						sql+=cd.oto.column()+" "+coltype+" , ";*/
			}
			if(current.pk!=null) {
				sql+=current.pk.name()+" "+current.pk.type()+" "+(current.pk.autoincrement()?"AUTO_INCREMENT":"")+" , ";
				sql+=" PRIMARY KEY ( "+current.pk.name()+" ) ";}
			if(foreign!=null&&foreign.pk!=null) {
				sql+=" , "+foreign.pk.name()+" "+foreign.pk.type();
			}
				
			sql+=");";
			System.out.println(sql);
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
	public CriteriaSet setCriteria(TableData table,TableHierarchyData hierarchy) {
		return new CriteriaSetMariaDb(this,table,hierarchy);
	}

	@Override
	public boolean updateTableForeignKey(TableData current,TableData foreign) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Object> select(CriteriaSet cs) throws ConstructorException, DbDriverNotFound, SecurityException, CommunicationException {
		try{
			Class.forName(this.driver);  
			Connection con=DriverManager.getConnection(  
			"jdbc:mysql://127.0.0.1:3306/"+database,username,password);   
			Statement stmt=con.createStatement(); 
			String sql = "SELECT * FROM " +cs.table.table.name();
			if(cs.hierarchy!=null)
				for(TableData td:cs.hierarchy.hierarchy)
					sql+=" , "+td.table.name();
			boolean crits=false;
			
			if(cs.crits.size()>0) {
				crits=true;
				sql+=" WHERE ";
				sql+=" "+cs.crits.get(0).getCriteria()+"  ";
				for(int i=1;i<cs.crits.size();i++)
					sql+=" AND "+cs.crits.get(i).getCriteria()+"  ";}
			if(cs.hierarchy!=null) {
				if(cs.hierarchy.hierarchy.size()>0) {
					if(crits)
						sql+=" AND ";
					else sql+=" WHERE ";
					sql+=cs.table.table.name()+"."+cs.hierarchy.hierarchy.get(0).pk.name()+"="+cs.hierarchy.hierarchy.get(0).table.name()+"."+cs.hierarchy.hierarchy.get(0).pk.name();
					for(int i=1;i<cs.hierarchy.hierarchy.size();i++) {
						TableData prev,curr;
						prev=cs.hierarchy.hierarchy.get(i-1);
						curr=cs.hierarchy.hierarchy.get(i);
						sql+=" AND "+prev.table.name()+"."+curr.pk.name()+"="+curr.table.name()+"."+curr.pk.name();}
				}
			}
			sql+=cs.getOrder();
			Constructor<?> cons=cs.table.class_name.getConstructor();
			Object ret;
			System.out.println(sql);
			ResultSet rs=stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int no_col=rsmd.getColumnCount();
			List<Object> retlo=new ArrayList<Object>();
			while(rs.next()) {
				ret=cons.newInstance();
				for(int i=1;i<=no_col;i++) {
					String col_name=rsmd.getColumnName(i);
					if(cs.table.pk!=null&&cs.table.pk.autoincrement()&&cs.table.pk.name().contentEquals(col_name)) {
					      Object obj=rs.getObject(i);
					      cs.table.pk_field.set(ret, obj);
					      continue;}
					for(ColumnData cd:cs.table.lcd) {
						if(cd.col!=null&&col_name.equals(cd.col.name())) {
							Object obj=rs.getObject(i);
							cd.f.set(ret, obj);
							break;}
						if(cs.table.pk!=null&&col_name.equals(cs.table.pk.name())) {
							Object obj=rs.getObject(i);
							cs.table.pk_field.set(ret, obj);
							break;}
					}
					for(TableData td:cs.hierarchy.hierarchy) {
					      if(td.pk!=null&&td.pk.autoincrement()&&td.pk.name().contentEquals(col_name)) {
						      Object obj=rs.getObject(i);
						      td.pk_field.set(ret,obj);
						      break;}
						for(ColumnData cd:td.lcd) {
							if(cd.col!=null&&col_name.equals(cd.col.name())) {
								Object obj=rs.getObject(i);
								cd.f.set(ret, obj);
								break;}
						}
					}
				}
				retlo.add(ret);
	      }
	      con.close();   
	      return retlo;
		}catch(NoSuchMethodException e) {
			e.printStackTrace();
			throw new ConstructorException();
		}catch (SQLException e) {
			e.printStackTrace();
			throw new CommunicationException();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new DbDriverNotFound();
		} catch (InstantiationException |IllegalAccessException|IllegalArgumentException |InvocationTargetException e) {
			e.printStackTrace();
			throw new ConstructorException();
		}
	}
	@Override
	public int insert(Object o,TableHierarchyValue hierarchy) {
		if(hierarchy==null)
			return -1;
		//for(TableData td:hierarchy)
			return this.insert(o,hierarchy.current,null,null);
	}

	@Override
	public int insert(Object o, TableValue table,PrimaryKey foreign,Object foreign_val) {
		try{  	/*if(table.pk!=null||table.pk_val!=null||(table.pk_val instanceof Integer&&(Integer)table.pk_val<1))
		      		throw new AutoIncrementValueException(table.pk.name());*/
			if(foreign!=null&&foreign_val!=null&&(foreign_val instanceof Integer&&(Integer)foreign_val<1))
	      			throw new AutoIncrementValueException(foreign.name());
			Class.forName(this.driver);  
			Connection con=DriverManager.getConnection(  
			"jdbc:mysql://127.0.0.1:3306/"+database,username,password);   
			Statement stmt=con.createStatement(); 
			String sql = "INSERT INTO " + table.table.name();
			String decl="",val="";
			decl+="(";
			val+="(";
			int start=0;
			boolean current_content=false;
			if(table.pk!=null) {
				if(table.pk.autoincrement()&&!(table.pk_field.getType().equals(int.class)||table.pk_field.getType().equals(Integer.class)))
					throw new AutoIncrementWrongTypeException(table.pk.name());
				if(!table.pk.autoincrement()&&(table.pk_field.getType().equals(int.class)||table.pk_field.getType().equals(Integer.class))&&(Integer)table.pk_val==0) {
					throw new AutoIncrementValueException(table.pk.name());
				}
				if((!table.pk.autoincrement())) {
					current_content=true;
					String quote=(table.pk_val instanceof String)?"'":"";
					decl+=" "+table.pk.name()+" ";
					val+=" "+quote+table.pk_val+quote+" ";}
			}
			else {
				
				ColumnValue cv=table.lcv.get(0);
				if(cv.col!=null) {
					current_content=true;
					start=1;
					String quote=(cv.value instanceof String)?"'":"";
					decl+=" "+cv.col.name()+" ";
					val+=" "+quote+cv.value+quote+" ";}
			}
			for(;start<table.lcv.size();start++) {
				ColumnValue cv=table.lcv.get(start);
				if(cv.col!=null) {
					current_content=true;
					if(start!=0) {
						decl+=" , ";
						val+=" , ";}
					String quote=(cv.value instanceof String)?"'":"";
					decl+=cv.col.name()+" ";
					val+=quote+cv.value+quote+" ";}
			}
			if(foreign!=null&&foreign_val!=null) {
				if(current_content) {
					decl+=" , ";
					val+=" , ";}
				String quote=(foreign_val instanceof String)?"'":"";
				decl+=foreign.name()+" ";
				val+=quote+foreign_val+quote+" ";
			}
			decl+=" ) ";
			val+=" ) ";
			sql+=decl+" VALUES "+val;
			System.out.println(sql);
	      stmt.executeUpdate(sql);
	      int insert_id=-1;
	      if(table.pk.autoincrement()) {
		    sql=" SELECT LAST_INSERT_ID(); ";
		    stmt=con.createStatement();
		    ResultSet rs=stmt.executeQuery(sql);
		    if(rs.next()) 
			  insert_id=rs.getInt(1);
	      }
	      con.close(); 
	      return insert_id;
			}catch(Exception e)
		{ e.printStackTrace();
		return -1;}
	}

}
