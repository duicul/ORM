package test;

import connector.DatabaseConnector;
import connector.MariaDBConnector;
import orm.ORMLoader;

public class Main {
 public static void main(String args[]) {
	 ORMLoader ol=new ORMLoader();
	 ol.get(Student.class,"name", "Popa");
	 DatabaseConnector dbc=new MariaDBConnector(3306,"127.0.0.1","root","", "advanced_databases");
	System.out.println(dbc.checktable("people"));
 }
}
