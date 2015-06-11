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
	
	private List<AccesPoint> _accesPoint;
	
	public MultiAccesPoint() {
		// TODO Auto-generated constructor stub
	}
	
	public void createAccesPoint(ConnexionInfo cInfo){
		AccesPoint ap = new AccesPoint(cInfo);
		ap.start();
		_accesPoint.add(ap);
		
	}
	
	
	public void update(Object obj){
		
		if(obj == null)
			return;
			
		if(obj instanceof AccesPoint)
			updateAccesPoint(obj);
		
	}
	
	private void updateAccesPoint(obj){
		
		
		
	}
	
	
	
}
