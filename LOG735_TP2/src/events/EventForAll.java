/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
�v�nement lanc� par les boutons "Envoyer � Tous"
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
