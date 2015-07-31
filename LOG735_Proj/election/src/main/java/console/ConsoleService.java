/**
 * 
 */
package console;

import java.io.IOException;

import org.apache.log4j.Logger;

import serverAccess.Commande;
import service.Service;

/**
 * @author MisterTim
 *
 */
public class ConsoleService extends Service {

	
	private final static Logger log = Logger.getLogger(ConsoleService.class);

	public ConsoleService(String serviceName) {
		super(serviceName);
		// TODO Auto-generated constructor stub
	}
	
	public void print(String mess){
		log.info(mess);
	}
	
	/* (non-Javadoc)
	 * @see service.Service#loopAction()
	 */
	@Override
	public void loopAction() {

		
		while(super.getCurrentState() != ServiceState.ENDING){
			try {
				Commande c = Commande.ParseCommande(System.in);
				if(c != null){
					this.setChanged();
					this.notifyObservers(c);
				}else{
					Thread.sleep(1000);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.debug("IOException",e);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.debug("InterruptedException",e);
			}
		}
		
		
	}

}
