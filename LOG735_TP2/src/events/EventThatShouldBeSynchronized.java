/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Événement lancé par les boutons "Envoi Synchronisé"
des Applications.

******************************************************/ 
package events;

public class EventThatShouldBeSynchronized extends EventForAll implements IEventSynchronized {

	private static final long serialVersionUID = 6603201529319860113L;

	public EventThatShouldBeSynchronized(String m){
		super(m);
	}
}
