package test;

import java.util.ArrayList;
import java.util.List;

import connector.Criteria;
import connector.CriteriaSet;
import connector.DatabaseConnector;
import connector.MariaDBConnector;
import exception.CommunicationException;
import exception.ConstructorException;
import exception.DbDriverNotFound;
import orm.ORMLoader;

public class Main {
 public static void main(String args[]) {
	 DatabaseConnector dbc=new MariaDBConnector(3306,"127.0.0.1","root","", "demo_orm");
	 //SELECT LAST_INSERT_ID();
	 ORMLoader ol=new ORMLoader(dbc);
	 //ol.get(Student.class,"name", "Popa");
	 CriteriaSet c=ol.setCriteria(StudentLiterature.class);
	 //c.lt("grade", 8);
	 //c.like("Name", "Gic%");
	 //c.orderAsc("grade");
	 List<Car> lc=new ArrayList<Car>();
	 lc.add(new Car("a","b","c"));
	 ol.insert(new People(null,"Fane"));
	 ol.insert(new StudentLiterature(lc,9, "Drama", "Gica"));
	 try {
		List<Object> ls=c.extract();
		System.out.println(ls);
		for(Object o:ls) {
		      StudentLiterature sl=(StudentLiterature) o;
			System.out.println(sl.name+" "+sl.grade+" "+sl.spec+" "+sl.pid+" "+sl.sid+" "+sl.slid);
		}
	} catch ( DbDriverNotFound | CommunicationException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	 
	 //ol.insert(new Student(null,5,"Popa"));
	 
	 /*try {
		System.out.println(dbc.checkTable("peoples"));
		ol.get(StudentLiterature.class,"name", "Popa");
	} catch (DbDriverNotFound | CommunicationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	Integer t=3;*/
 }
}
