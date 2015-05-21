/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
Classe d�crivant la base des �v�nements transmis.
******************************************************/ 
package events;

public class EventBase implements IEvent {

	private static final long serialVersionUID = 5483268778788190805L;
	
	private String message;
	
	public EventBase(String message){
		this.message = message;
	}
	
	public String getMessage() { return message; }
}
