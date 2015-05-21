/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Exécutable qui démarre le bus d'événements sur le
port 12045. Ne devrait pas être modifié.
******************************************************/ 
package eventbus;

public class MainEventBus {

	public static void main(String[] args) {
		EventBusThread eventBus = new EventBusThread(12045);
		eventBus.start();
	}
}
