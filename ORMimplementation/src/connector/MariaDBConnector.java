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
import annotations.Table;
import exception.AutoIncrementValueException;
import exception.AutoIncrementWrongTypeException;
import exception.CommunicationException;
import exception.ConstructorException;
import exception.DbDriverNotFound;
import orm.ColumnData;
import orm.ColumnValue;
import orm.TableData;
import orm.TableHierarchyData;
import orm.TableValue;

public class MariaDBConnector extends DatabaseConnector {
	public final String driver;
	public MariaDBConnector(long port, String hostname, String username, String password, String database,boolean show_querries) {
		super(port, hostname, username, password, database, show_querries);
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
			if(this.show_querries)
			      System.out.println(sql);
	      stmt.executeUpdate(sql);
	      con.close();   
	      return true;
			}catch(Exception e)
		{ e.printStackTrace();
		return false;}
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
	
	public List<Object> select(){
	    return null;}
	
	@Override
	public List<Object> projection(List<TableData> hierarchy,List<Criteria> criters,TableData current_table,String order,boolean remove) throws ConstructorException, DbDriverNotFound, SecurityException, CommunicationException {
		try{
			Class.forName(this.driver);  
			Connection con=DriverManager.getConnection(  
			"jdbc:mysql://127.0.0.1:3306/"+database,username,password);   
			Statement stmt=con.createStatement();
			String select_querry="SELECT * ";
			String delete_querry=" DELETE ";
			String sql = " FROM ";
			if(hierarchy!=null) {
			      	boolean first=true;
				for(TableData td:hierarchy)
				      if(td.table!=null) {
					    if(!first) {
						  sql+=" , ";
						  delete_querry+=" , ";}
					sql+=td.table.name();
					delete_querry+=td.table.name();
					first=false;}
			}
			boolean crits=false;
			
			if(criters.size()>0) {
				crits=true;
				sql+=" WHERE ";
				sql+=" "+criters.get(0).getCriteria()+"  ";
				for(int i=1;i<criters.size();i++)
					sql+=" AND "+criters.get(i).getCriteria()+"  ";}
			if(hierarchy!=null) {
				if(hierarchy.size()>1) {
					if(crits)
						sql+=" AND ";
					else sql+=" WHERE ";
					TableData tdmain=hierarchy.get(0),tdmain1=hierarchy.get(1);
					if(tdmain!=null)
					      sql+=tdmain.table.name()+"."+tdmain1.pk.name()+"="+tdmain1.table.name()+"."+tdmain1.pk.name();
					for(int i=2;i<hierarchy.size();i++) {
						TableData prev,curr;
						prev=hierarchy.get(i-1);
						curr=hierarchy.get(i);
						if(prev!=null&&curr!=null)
						      sql+=" AND "+prev.table.name()+"."+curr.pk.name()+"="+curr.table.name()+"."+curr.pk.name();}
				}
			}
			sql+=order==null?"":order;
			Constructor<?> cons=current_table.class_name.getConstructor();
			Object ret;
			if(show_querries)
			      System.out.println(select_querry+sql);
			ResultSet rs=stmt.executeQuery(select_querry+sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int no_col=rsmd.getColumnCount();
			List<Object> retlo=new ArrayList<Object>();
			while(rs.next()) {
				ret=cons.newInstance();
				for(int i=1;i<=no_col;i++) {
					String col_name=rsmd.getColumnName(i);
					if(current_table.pk!=null&&current_table.pk.autoincrement()&&current_table.pk.name().contentEquals(col_name)) {
					      Object obj=rs.getObject(i);
					      current_table.pk_field.set(ret, obj);
					      continue;}
					for(ColumnData cd:current_table.lcd) {
						if(cd.col!=null&&col_name.equals(cd.col.name())) {
							Object obj=rs.getObject(i);
							cd.f.set(ret, obj);
							break;}
						if(current_table.pk!=null&&col_name.equals(current_table.pk.name())) {
							Object obj=rs.getObject(i);
							current_table.pk_field.set(ret, obj);
							break;}
					}
					for(TableData td:hierarchy) {
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
	      if(remove) {
		    if(show_querries)
			      System.out.println(delete_querry+sql);
		    stmt.executeUpdate(delete_querry+sql);
	      }
			
	      con.close();   
	      return retlo;
		}catch(NoSuchMethodException e) {
			e.printStackTrace();
			throw new ConstructorException(current_table.class_name.toString());
		}catch (SQLException e) {
			e.printStackTrace();
			throw new CommunicationException();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new DbDriverNotFound();
		} catch (InstantiationException |IllegalAccessException|IllegalArgumentException |InvocationTargetException e) {
			e.printStackTrace();
			throw new ConstructorException(current_table.class_name.toString());
		}
	}

	@Override
	public int insert(Object o, TableValue table, Pair<PrimaryKey,Object> foreign_hie,Pair<PrimaryKey,Object> foreign_comp) {
		try{  	if(foreign_hie.l!=null&&foreign_hie.r!=null&&(foreign_hie.r instanceof Integer&&(Integer)foreign_hie.r<1))
	      			throw new AutoIncrementValueException(foreign_hie.l.name());
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
			if(foreign_hie.l!=null&&foreign_hie.r!=null) {
				if(current_content) {
					decl+=" , ";
					val+=" , ";}
				String quote=(foreign_hie.r instanceof String)?"'":"";
				decl+=foreign_hie.l.name()+" ";
				val+=quote+foreign_hie.r+quote+" ";
			}
			if(foreign_comp!=null)
			      if(foreign_comp.l!=null&&foreign_comp.r!=null) {
				    if(current_content) {
						decl+=" , ";
						val+=" , ";}
					String quote=(foreign_comp.r instanceof String)?"'":"";
					decl+=foreign_comp.l.name()+" ";
					val+=quote+foreign_comp.r+quote+" ";
			      }
			decl+=" ) ";
			val+=" ) ";
			sql+=decl+" VALUES "+val;
			if(show_querries)
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

      @Override
      public boolean dropTable(List<Table> t) {
	    
	    try {Class.forName(this.driver);  
		Connection con;
		  con = DriverManager.getConnection(  
		"jdbc:mysql://127.0.0.1:3306/"+database,username,password);
		  Statement stmt=con.createStatement();
		 String sql="";
		 for(Table ti:t) {
		       sql=" DROP TABLE IF EXISTS `"+ti.name()+" ; ";
		       if(show_querries)
			     System.out.println(sql);
		       stmt.executeUpdate(sql);
		       }
		return true;
	    } catch (SQLException | ClassNotFoundException e) {
		  // TODO Auto-generated catch block
		e.printStackTrace();
	    }   
		 
		
		
	    return false;
      }

      @Override
      public boolean cleanTable(List<Table> t) {
	    // TODO Auto-generated method stub
	    return false;
      }

}
