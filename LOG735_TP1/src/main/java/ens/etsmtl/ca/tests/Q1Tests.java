/**
 * 
 */
package ens.etsmtl.ca.tests;




import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ens.etsmtl.ca.q1.Client;
import ens.etsmtl.ca.q1.Server;

/**
 * @author AJ98150
 *
 */
public class Q1Tests {

	private static BufferedReader serv1_outputStream;
	private static BufferedReader serv1_errOutputStream;
	private static	PrintWriter serv1_inputStream;
	
	private static	BufferedReader client1_outputStream;
	private static	BufferedReader client1_errOutputStream;
	private static PrintWriter client1_inputStream;

	
	
	private final static String q1_log_dir = "./tests/log/q1/";
	
	private final static String serv1_outputFileStream_Name = q1_log_dir + "/serv1_outputFileStream";
	private final static String serv1_errOutputFileStream_Name = q1_log_dir + "/serv1_errOutputFileStream";
	private final static String serv1_intputFileStream_Name = q1_log_dir + "/serv1_inputFileStream";
	
	private final static String client1_outputFileStream_Name = q1_log_dir + "/client1_outputFileStream";
	private final static String client1_errOutputFileStream_Name = q1_log_dir + "/client1_errOutputFileStream";
	private final static String client1_inputFileStream_Name = q1_log_dir + "/client1_inputFileStream";
	
	
	
	private static ScheduledExecutorService executor;
	
	
	/**
	 * Create file if not exist
	 * @param pathName
	 * @return
	 * @throws IOException
	 */
	private static File PrepareFile(String pathName) throws IOException{
		File file = new File(pathName);
		
		//http://stackoverflow.com/questions/9620683/java-fileoutputstream-create-file-if-not-exists
		if(!file.exists())
			file.createNewFile();
		
		return file;
	}
	
	/**
	 * Create all directories needed if they not exist
	 */
	private static void PrepareDirectories() {
		File file = new File(q1_log_dir);
		
		if(!file.exists())
			file.mkdirs();
	}
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		PrepareDirectories();
		
		FileInputStream serv1_outputFileStream = new FileInputStream(PrepareFile(serv1_outputFileStream_Name));
		FileInputStream serv1_errOutputFileStream = new FileInputStream(PrepareFile(serv1_errOutputFileStream_Name));
		
		FileInputStream client1_outputFileStream = new FileInputStream(PrepareFile(client1_outputFileStream_Name));
		FileInputStream client1_errOutputFileStream = new FileInputStream(PrepareFile(client1_errOutputFileStream_Name));
		
		
		serv1_outputStream = new BufferedReader(new InputStreamReader(serv1_outputFileStream));
		serv1_errOutputStream = new BufferedReader(new InputStreamReader(serv1_errOutputFileStream));
		serv1_inputStream = new PrintWriter(PrepareFile(serv1_intputFileStream_Name));
		
		client1_outputStream = new BufferedReader(new InputStreamReader(client1_outputFileStream));
		client1_errOutputStream = new BufferedReader(new InputStreamReader(client1_errOutputFileStream));
		client1_inputStream = new  PrintWriter(PrepareFile(client1_inputFileStream_Name));
		
		
		executor = Executors.newScheduledThreadPool(2);
		
	}
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownClass() throws Exception {
		serv1_outputStream.close();
		serv1_errOutputStream.close();
		serv1_inputStream.close();
		
		client1_outputStream.close();
		client1_errOutputStream.close();
		client1_inputStream.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		

		final String[] ServerStreams = {serv1_intputFileStream_Name,serv1_outputFileStream_Name,serv1_errOutputFileStream_Name};
		final String[] ClientStreams = {client1_inputFileStream_Name,client1_outputFileStream_Name,client1_errOutputFileStream_Name};
		
		Thread serverThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Server.main(ServerStreams);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		
		Thread clientThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Client.main(ClientStreams);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		serverThread.start();
		//clientThread.start();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testServerBindIp() {
		
		serv1_inputStream.println("127.0.0.1");
		
		
		final Future<String> error_handler = executor.submit(new Callable<String>() {
		    @Override
		    public String call() throws Exception {
		    	String res = serv1_errOutputStream.readLine();
		    	executor.shutdownNow();
				return res;
		    }
		});
		
		Future<String> handler = null;
		
		try{
			handler = executor.submit(new Callable<String>() {
			    @Override
			    public String call() throws Exception {
			    	String res = serv1_outputStream.readLine();
			    	error_handler.cancel(true);
			    	executor.shutdownNow();
					return res;
			    }
			});
		
		}catch(RejectedExecutionException e){
			
		}
		
		
		
		while(!executor.isTerminated());
		
		if(error_handler.isCancelled())
		{
			try {
				assertTrue(handler.get(), handler.isDone());
			} catch (InterruptedException | ExecutionException e) {
				fail("serv1_output handdler Exception \n" + e.getStackTrace());
			}
		}
		
		if(handler != null && handler.isCancelled() || handler == null)
		{
			try {
				fail(error_handler.get());
			} catch (InterruptedException | ExecutionException e) {
				fail("serv1_errOutput handdler Exception \n" + e.getStackTrace());
			}
		}
		
		
	}

}
