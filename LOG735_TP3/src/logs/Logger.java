package logs;

import java.io.PrintStream;

public class Logger {
	
	private static PrintStream defaultPrintStream = System.out;
	
	public static Logger createLog(Class<?> c)
	{
		return new Logger(c);
	}
	
	private final Class<?> classloged;
	
	private Logger(Class<?> c) {
		classloged = c;
	}
	
	public void message(String message)
	{
		defaultPrintStream.println(classloged.getCanonicalName() + " : " + message);
	}

	
	
}
