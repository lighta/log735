/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
�v�nement lanc� par les boutons "Envoyer � App Deux"
des Applications.
******************************************************/ 
package events;

public class EventForPartTwo extends EventBase implements IPartTwoEvent{

	private static final long serialVersionUID = 286636600291399763L;

	public EventForPartTwo(String m){
		super(m);
	}

}
