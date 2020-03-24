package test;

import connector.Criteria;
import connector.DatabaseConnector;
import connector.MariaDBConnector;
import exception.CommunicationException;
import exception.DbDriverNotFound;
import orm.ORMLoader;

public class Main {
 public static void main(String args[]) {
	 DatabaseConnector dbc=new MariaDBConnector(3306,"127.0.0.1","root","", "demo_orm");
	 ORMLoader ol=new ORMLoader(dbc);
	 //ol.get(Student.class,"name", "Popa");
	 Criteria c=ol.setCriteria(Student.class);
	 
	try {
		System.out.println(dbc.checkTable("peoples"));
		ol.get(StudentLiterature.class,"name", "Popa");
	} catch (DbDriverNotFound | CommunicationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	Integer t=3;
 }
}
