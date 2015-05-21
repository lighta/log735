/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Événement lancé par les boutons "Envoyer à App Un"
des Applications.
******************************************************/ 
package events;

public class EventForPartOne extends EventBase implements IPartOneEvent{

	private static final long serialVersionUID = -6784739853826767178L;

	public EventForPartOne(String m){
		super(m);
	}
}
