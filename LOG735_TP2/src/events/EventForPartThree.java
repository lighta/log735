/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
�v�nement lanc� par les boutons "Envoyer � App Trois"
des Applications.
******************************************************/ 
package events;

public class EventForPartThree extends EventBase implements IPartThreeEvent {

	private static final long serialVersionUID = -854026869036649703L;

	public EventForPartThree(String m){
		super(m);
	}
}
