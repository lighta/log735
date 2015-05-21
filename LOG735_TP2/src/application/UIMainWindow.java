/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Interface graphique des applications simulées. MainPartOne,
MainPartTwo et MainPartThree instancient cette classe.

L'interface offre les fonctionnalités suivantes :
-Envoyer à App Un/Deux/Trois : envoie l'événement associé
 de l'application source à l'application de destination.
-Envoyer à Tous : envoie l'événement associé
 de l'application source aux deux autres applications.
-Envoi Synchronisé : envoie l'événement qui doit être
synchronisé à toutes les applications.

NOTE : Seules les classes internes implémentant ActionListener
situées à la fin de la classe ont le potentiel de nécessiter 
des modifications.
******************************************************/ 
package application;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import events.*;

public class UIMainWindow extends JFrame implements IObserver {

	private static final long serialVersionUID = 17889303454552887L;
	
	private int delay; //Temps artificiel de délai de traitement des événements
	private String syncText; //Texte à afficher lors de l'événement synchronisé
	
	private JList lstResultatEvent;
	private DefaultListModel model;
	private JScrollPane scrollPane;
	
	//Construit l'interface graphique.
	//Ne devrait pas être modifié.
	public UIMainWindow(IEventBusConnector eventBusConn, String name, String syncText, int delay) {
		super();
		this.delay = delay;
		this.syncText = syncText;
		setSize(450,480);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(name);
		getContentPane().setLayout(null);
		setResizable(false);

		model = new DefaultListModel(); 
		lstResultatEvent = new JList(model);
		scrollPane = new JScrollPane();
		JButton sendToPartOne = new JButton();
		JButton sendToPartTwo = new JButton();
		JButton sendToPartThree = new JButton();
		JButton sendToAll = new JButton();
		JButton sendSynchroToAll = new JButton();
		
		scrollPane.getViewport().setView(lstResultatEvent);
		sendToPartOne.setText("Envoyer à App Un");
		sendToPartTwo.setText("Envoyer à App Deux");
		sendToPartThree.setText("Envoyer à App Trois");
		sendToAll.setText("Envoyer à Tous");
		sendSynchroToAll.setText("Envoie Synchronisé");

		sendToPartOne.addActionListener(new PartOneActionListener(name, eventBusConn));
		sendToPartTwo.addActionListener(new PartTwoActionListener(name, eventBusConn));
		sendToPartThree.addActionListener(new PartThreeActionListener(name, eventBusConn));
		sendToAll.addActionListener(new AllActionListener(name, eventBusConn));
		sendSynchroToAll.addActionListener(new AllSynchroActionListener(name, eventBusConn));
		
		// Une couleur exagérée pour être sûr que tu comprennes 
		// que c'est le bouton important du laboratoire :)
		sendSynchroToAll.setBackground(Color.CYAN);

		sendToPartOne.setBounds(10, 10, 200, 30);
		sendToPartTwo.setBounds(10, 50, 200, 30);
		sendToPartThree.setBounds(10, 90, 200, 30);
		sendToAll.setBounds(220, 10, 200, 30);
		sendSynchroToAll.setBounds(220, 50, 200, 70);
		scrollPane.setBounds(10, 130, 420, 300);

		add(sendToPartOne);
		add(sendToPartTwo);
		add(sendToPartThree);
		add(sendToAll);
		add(sendSynchroToAll);
		add(scrollPane);
	}
	

	//Affichage du message contenu dans les événements reçus
	//par utilisation du patron Observer.
	//Si l'événement est de type IEventSynchronized,
	//affiche le texte contenu dans syncText.
	
	public void update(Object o, Object arg) {
		
		System.out.println("Réception de l'événement: " + arg.toString());
		IEvent event = (IEvent)arg;
		try {
			Thread.sleep(1000*delay);
		}
		catch(InterruptedException ie) {
			ie.printStackTrace();
		}
		
		if(event instanceof IEventSynchronized) {
			model.addElement(syncText);
		}
		else {
			model.addElement(event.toString() + " - " + event.getMessage());
		}
	}
}

class PartOneActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public PartOneActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) {
		eventBusConn.callEvent(new EventForPartOne(name));
	}
}

class PartTwoActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public PartTwoActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) {
		eventBusConn.callEvent(new EventForPartTwo(name));
	}
}

class PartThreeActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public PartThreeActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) {
		eventBusConn.callEvent(new EventForPartThree(name));
	}
}

class AllActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public AllActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) {
		eventBusConn.callEvent(new EventForAll(name));
	}
}

class AllSynchroActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public AllSynchroActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) {
		eventBusConn.callEvent(new EventThatShouldBeSynchronized(name));
	}
}