package test;

public class FileLockingTest {

      public static void main(String[] args) {
	  for(int i=0;i<50;i++)
		new TestWrite(i).start();  

      }

}
