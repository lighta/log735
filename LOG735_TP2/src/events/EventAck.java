package events;

public class EventAck extends EventForAll implements IEventAck {

	private static final long serialVersionUID = -1156382431931313979L;

	public EventAck(String m){
		super(m);
	}
}