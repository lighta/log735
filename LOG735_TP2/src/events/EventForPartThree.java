/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Événement lancé par les boutons "Envoyer à App Trois"
des Applications.
******************************************************/ 
package events;

public class EventForPartThree extends EventBase implements IPartThreeEvent {

	private static final long serialVersionUID = -854026869036649703L;

	public EventForPartThree(String m){
		super(m);
	}
}
