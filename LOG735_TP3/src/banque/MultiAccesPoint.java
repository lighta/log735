/**
 * 
 */
package banque;

import java.util.List;

/**
 * @author AJ98150
 *
 */
public class MultiAccesPoint extends Observable {
	
	private Map<String,AccesPoint> _accesPoint;
	
	public MultiAccesPoint() {
		// TODO Auto-generated constructor stub
		
		_accesPoint = new HashMap<String,AccesPoint>();
	}
	
	public void createAccesPoint(String name,ConnexionInfo cInfo){
		AccesPoint ap = new AccesPoint(name,cInfo);
		ap.start();
		_accesPoint.add(name,ap);
		
	}
	
	
	public void update(Object obj){
		
		if(obj == null)
			return;
			
		if(obj instanceof AccesPoint){
			if()
			messageReceiveFrom(obj);
		
		
		}
	}
	
	protected abstract void newConnexionFrom(AccesPoint ap);
	protected abstract void messageReceiveFrom(AccesPoint ap);
	
	protected AccesPoint getAccesPointByName(String name){
		return _accesPoint.getValue(name);
	}
	
	
}
