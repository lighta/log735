package service;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;



/**
 * Classe gerant un ensemble de service
 * service are like deamon, you can start and stop them
 * serviceName must be unique
 * @author MisterTim
 *
 */
public abstract class Service extends Observable {

	private static final Logger log = Logger.getLogger(Service.class);
	
	private static Map<String,Service> serviceMap = null;
	private static Map<String,Thread> serviceThread = null;
	
	static {
		synchronized(Service.class){
			if(serviceMap == null)
				serviceMap = new HashMap<String,Service>();
			if(serviceThread == null)
				serviceThread = new HashMap<String,Thread>();
		}
	}
	
	/**
	 * Start a service
	 * @param s : service to start
	 * @throws AlreadyStartException
	 */
	public static void startService(Service s) throws AlreadyStartException{
		
		log.debug("Try to start service ( " + s.serviceName + " )");	
		
		
		if(serviceMap.containsKey(s.getName()) && 
				(s.getCurrentState() == ServiceState.ENDED || s.getCurrentState() == ServiceState.NOT_STARTED )){
			s.setCurrentState(ServiceState.STARTING);
			Thread th = new Thread(s.serviceLoopAction);
			th.start();
			synchronized (serviceThread) {
				serviceThread.put(s.serviceName, th);
			}
			log.debug("starting service ( " + s.serviceName + " )");
		}
		else{
			log.debug("already started service ( " + s.serviceName + " )");
			throw new AlreadyStartException("Service is already started");
		}
		
	}

	/**
	 * stop a service
	 * @param s : service to stop
	 */
	public static void stopService(Service s){
		log.debug("Stopping service ( " + s.serviceName + " )");
		s.setCurrentState(ServiceState.ENDING);
	}
		
	/**
	 * Available type for a service
	 * @author MisterTim
	 *
	 */
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
	
	/**
	 * Creation et lancement d'un service
	 * @param serviceName
	 */
	public Service(final String serviceName) {
		this.serviceName = serviceName;
		
		currentState = ServiceState.NOT_STARTED;
		serviceLoopAction = new Runnable() {
			
			@Override
			public void run() {
				setCurrentState(ServiceState.STARTED);
				log.debug("started service ( " + serviceName + " )");
				loopAction();
				setCurrentState(ServiceState.ENDED);
				log.debug("ended service ( " + serviceName + " )");
			}
		};
		synchronized (serviceMap) {
			serviceMap.put(serviceName, this);
		}
		
	}

	/**
	 * Donne le nom du service
	 * @return serviceName
	 */
	public String getName() {
		return serviceName;
	}
	
	/**
	 * Donne l'etat actuelle du service cf (enum ServiceState)
	 * @return currentState
	 */
	public ServiceState getCurrentState() {
		return currentState;
	}

	/**
	 * Definit l'etat actuelle d'un service
	 * @param currentState
	 */
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
