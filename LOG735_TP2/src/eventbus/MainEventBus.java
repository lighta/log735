/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
Ex�cutable qui d�marre le bus d'�v�nements sur le
port 12045. Ne devrait pas �tre modifi�.
******************************************************/ 
package eventbus;

public class MainEventBus {

	public static void main(String[] args) {
		EventBusThread eventBus = new EventBusThread(12045);
		eventBus.start();
	}
}
