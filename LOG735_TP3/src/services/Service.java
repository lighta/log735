package services;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import logs.Logger;

public abstract class Service extends Observable {

	private static final Logger log = Logger.createLog(Service.class);
	
	private static Map<String,Service> serviceMap = null;
	
	static {
		synchronized(Service.class){
			if(serviceMap == null)
				serviceMap = new HashMap<String,Service>();
		}
	}
	
	public static void startService(Service s) throws AlreadyStartException{
		
		log.message("Try to start service ( " + s.serviceName + " )");	
		
		
		if(serviceMap.containsKey(s.getName()) && 
				(s.getCurrentState() == ServiceState.ENDED || s.getCurrentState() == ServiceState.NOT_STARTED )){
			s.setCurrentState(ServiceState.STARTING);
			(new Thread(s.serviceLoopAction)).start();
			log.message("starting service ( " + s.serviceName + " )");
		}
		else{
			log.message("already started service ( " + s.serviceName + " )");
			throw new AlreadyStartException("Service is already started");
		}
		
	}

	public static void stopService(Service s){
		log.message("Stopping service ( " + s.serviceName + " )");
		s.setCurrentState(ServiceState.ENDING);
	}
	
	
	public enum ServiceState {
		STARTING,
		STARTED,
		ENDING,
		ENDED,
		NOT_STARTED
	}
	
	private String serviceName;
	private Runnable serviceLoopAction;
	private ServiceState currentState;
	
	public Service(final String serviceName) {
		
		this.serviceName = serviceName;
		
		currentState = ServiceState.NOT_STARTED;
		serviceLoopAction = new Runnable() {
			
			@Override
			public void run() {
				setCurrentState(ServiceState.STARTED);
				log.message("started service ( " + serviceName + " )");
				loopAction();
				setCurrentState(ServiceState.ENDED);
				log.message("ended service ( " + serviceName + " )");
			}
		};
		synchronized (serviceMap) {
			serviceMap.put(serviceName, this);
		}
		
	}

	public String getName() {
		return serviceName;
	}
	
	public ServiceState getCurrentState() {
		return currentState;
	}

	private void setCurrentState(ServiceState currentState) {
		this.currentState = currentState;
	}
	
	/**
	 * define here your loop action
	 * @usage {@code} while(super.getCurrentState != ServiceState.ENDING) { 
	 * 					do your actions } {@code}
	 */
	public abstract void loopAction();
	
	public static class ServiceException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public ServiceException(String message) {
			super(message);
		}
		
	}
	

	public static class AlreadyStartException extends ServiceException
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public AlreadyStartException(String message) {
			super(message);
		}
		
	}
	

	
}
