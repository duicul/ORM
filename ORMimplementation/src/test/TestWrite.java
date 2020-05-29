package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

public class TestWrite extends Thread{
      
      int val;
      public TestWrite(int t) {
	    this.val=t;
      }
      
      public void run() {
	    FileOutputStream fos = null;
	    String data="";
	    try {
		  
		  fos = new FileOutputStream("file.txt",true);
		  FileLock fl=null;
		  try{
			fl = fos.getChannel().tryLock();
		  }catch(OverlappingFileLockException e) {
			e.printStackTrace();
			fl=null;
		  }
		  while(fl==null) {
			Thread.sleep(100);
			try{
			      fl = fos.getChannel().tryLock();
			}catch(OverlappingFileLockException e) {e.printStackTrace();}
		  }
		    if(fl != null) {
		      System.out.println("Locked File "+val);
		      fos.write((data+val+"\n").getBytes());
		      fl.release();
		      System.out.println("Released Lock "+val);
		    }
		    
	    } catch ( IOException | InterruptedException e) {
		  // TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    finally {try {
		 if(fos!=null)
		       fos.close();
	    } catch (IOException e) {
		  // TODO Auto-generated catch block
		e.printStackTrace();
	    }
		  
	    }
	   
      }

}
