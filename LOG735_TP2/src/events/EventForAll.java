/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Événement lancé par les boutons "Envoyer à Tous"
des Applications.
******************************************************/ 
package events;

public class EventForAll extends EventBase implements IPartOneEvent, IPartThreeEvent,
		IPartTwoEvent {

	private static final long serialVersionUID = -2570010132007741935L;

	public EventForAll(String m){
		super(m);
	}
}
