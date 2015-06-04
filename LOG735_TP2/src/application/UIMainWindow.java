/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
Interface graphique des applications simul�es. MainPartOne,
MainPartTwo et MainPartThree instancient cette classe.

L'interface offre les fonctionnalit�s suivantes :
-Envoyer � App Un/Deux/Trois : envoie l'�v�nement associ�
 de l'application source � l'application de destination.
-Envoyer � Tous : envoie l'�v�nement associ�
 de l'application source aux deux autres applications.
-Envoi Synchronis� : envoie l'�v�nement qui doit �tre
synchronis� � toutes les applications.

NOTE : Seules les classes internes impl�mentant ActionListener
situ�es � la fin de la classe ont le potentiel de n�cessiter 
des modifications.
******************************************************/ 
package application;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import events.*;

public class UIMainWindow extends JFrame implements IObserver {

	private static final long serialVersionUID = 17889303454552887L;
	
	private int delay; //Temps artificiel de d�lai de traitement des �v�nements
	private String syncText; //Texte � afficher lors de l'�v�nement synchronis�
	
	private JList lstResultatEvent;
	private DefaultListModel model;
	private JScrollPane scrollPane;
	
	static int id;
	private class Sync_ele {
		String msg;			//message a afficher
		boolean end;		//si notre message est la fin de la chaine
		int num_ack;		//numero d'ack, pour eviter les doublon
		int cur_ack;		//compteur d'ack avant d'afficher notre msg
		
		public Sync_ele(String msg, boolean end, int num_ack) {
			super();
			this.msg = msg;
			this.end = end;
			this.cur_ack = this.num_ack = num_ack;
		}
	}
	private HashMap<Integer, Sync_ele> list_syncMsg = new HashMap();
	IEventBusConnector eventBusConn;
	
	//Construit l'interface graphique.
	//Ne devrait pas �tre modifi�.
	public UIMainWindow(IEventBusConnector eventBusConn, String name, String syncText, int delay) {
		super();
		this.delay = delay;
		this.syncText = syncText;
		this.eventBusConn = eventBusConn;
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
		sendToPartOne.setText("Envoyer � App Un");
		sendToPartTwo.setText("Envoyer � App Deux");
		sendToPartThree.setText("Envoyer � App Trois");
		sendToAll.setText("Envoyer � Tous");
		sendSynchroToAll.setText("Envoie Synchronis�");

		sendToPartOne.addActionListener(new PartOneActionListener(name, eventBusConn));
		sendToPartTwo.addActionListener(new PartTwoActionListener(name, eventBusConn));
		sendToPartThree.addActionListener(new PartThreeActionListener(name, eventBusConn));
		sendToAll.addActionListener(new AllActionListener(name, eventBusConn));
		sendSynchroToAll.addActionListener(new AllSynchroActionListener(name, eventBusConn));
		
		// Une couleur exag�r�e pour �tre s�r que tu comprennes 
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
	

	//Affichage du message contenu dans les �v�nements re�us
	//par utilisation du patron Observer.
	//Si l'�v�nement est de type IEventSynchronized,
	//affiche le texte contenu dans syncText.
	
	public void update(Object o, Object arg) {
		
		System.out.println("R�ception de l'�v�nement: " + arg.toString());
		IEvent event = (IEvent)arg;
		try {
			Thread.sleep(1000*delay);
		}
		catch(InterruptedException ie) {
			ie.printStackTrace();
		}
		
		if(event instanceof IEventAckFin) {
			//split
			String ackmsg = event.getMessage();
			String[] parts = ackmsg.toString().split("#");
			int id = Integer.parseInt(parts[0]);
			Sync_ele ele = list_syncMsg.get(id);
			if(ele == null) //this msg ain't active for us
				;
			else {
				//delete id	
				list_syncMsg.remove(id);
				if(list_syncMsg.isEmpty() == false){
					do { //search mnext
						ele = list_syncMsg.get(id+1 % list_syncMsg.size());
					} while(ele == null); 
					if(ele.num_ack==0){
						model.addElement(ele.msg);
						eventBusConn.callEvent(new EventAck(id+"#"+ele.num_ack));
					}
				}
			}
		}
		else if(event instanceof IEventAck) {
			//split
			String ackmsg = event.getMessage();
			String[] parts = ackmsg.toString().split("#");
			int id = Integer.parseInt(parts[0]);
			System.out.println("Reception d'un ACK:"+ackmsg);
			
			Sync_ele ele = list_syncMsg.get(id);
			if(ele == null) //this msg ain't active for us
				System.out.println("id="+id+" not found in list");
			else if (ele.cur_ack > 0) {
				//decrement
				System.out.println("cur_ACK="+ele.cur_ack+" num_ACK="+ele.num_ack);
				ele.cur_ack--;
				list_syncMsg.replace(id, ele); //update
				//affiche ou attend
				if(ele.cur_ack==0)
					model.addElement(ele.msg);
				if(ele.end)
					eventBusConn.callEvent(new EventAckFin(id+""));
				else
					eventBusConn.callEvent(new EventAck(id+"#"+ele.num_ack));
				//si affiche ack ou ackfin
			}
		}
		else if(event instanceof IEventSynchronized) {
			//split
			String[] parts = syncText.toString().split("#");
			id++;
			int num_ack = Integer.parseInt(parts[0]);
			int tot_ack = Integer.parseInt(parts[1]);
			String msg = parts[2];
			boolean end = (num_ack == tot_ack);
			
			//store id {---;---}
			Sync_ele ele = new Sync_ele(msg,end,num_ack);
			list_syncMsg.put(id, ele);
			
			//si acknumber affiche et ack
			if(num_ack==0){
				model.addElement(msg);
				eventBusConn.callEvent(new EventAck(id+"#"+ele.num_ack));
			}
			//sinon attend
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