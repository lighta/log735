/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
Ex�cutable de Application Un. � l'ex�cution, l'objet
tente de se connecter � [IP]:12045 pour �tablir
un lien avec le bus d'�v�nements.

D�lai de traitement des �v�nements : 3 secondes.
�v�nement synchronis� : affiche le mot "Vous".
******************************************************/ 

package application;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import events.IEventSynchronized;
import events.IPartOneEvent;

public class MainPartOne {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String ip = JOptionPane.showInputDialog("Entrez le IP du EventBus", "127.0.0.1");

		List<Class> listenedEvents = new ArrayList<Class>();
		listenedEvents.add(IPartOneEvent.class);
		listenedEvents.add(IEventSynchronized.class);
		EventBusConnector bus = new EventBusConnector(listenedEvents, ip, 12045);
		UIMainWindow window = new UIMainWindow(bus, "App Un", "0#2#Vous", 3);
		bus.addObserver(window);
		
		bus.start();
		window.setVisible(true);
	}

}
