/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Événement lancé par les boutons "Envoyer à App Deux"
des Applications.
******************************************************/ 
package events;

public class EventForPartTwo extends EventBase implements IPartTwoEvent{

	private static final long serialVersionUID = 286636600291399763L;

	public EventForPartTwo(String m){
		super(m);
	}

}
