package events;

public class EventAckFin extends EventForAll implements IEventAckFin {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1156382431931313979L;

	public EventAckFin(String m){
		super(m);
	}
}