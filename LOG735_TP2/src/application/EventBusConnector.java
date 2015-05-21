/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Classe qui gère la transmission et la réception
d'événements du côté d'une instance d'Application.

La classe est en constante attente de nouveaux événements
à l'aide d'un Thread. Lorsque l'Application associée
au Connector lui envoie un événement, le Connector
envoie l'événement au bus à l'aide d'un second Thread.
******************************************************/ 
package application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import events.IEvent;


public class EventBusConnector extends Thread implements IEventBusConnector {
	// Liste des événements à écouter.
	@SuppressWarnings("unchecked")
	private List<Class> listenedEvents;
	
	private List<IEvent> lstEventsToSend = new ArrayList<IEvent>();
	private List<IObserver> lstObserver = new ArrayList<IObserver>();
	private Socket s;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private ReadEventFromStream readStream;
	
	@SuppressWarnings("unchecked")
	public EventBusConnector(List<Class> listenedEvents, String ip, int port) {
		this.listenedEvents = listenedEvents;

		try {
			s = new Socket(ip, port);
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
			readStream = new ReadEventFromStream(ois, this);
		}
		catch(IOException ioe) {
			System.out.println("Impossible de se connecter au serveur.");
			System.exit(1);
		}
	}
	
	public void start() {
		super.start();
		readStream.start();
	}
	
	//Thread qui envoie au bus d'événements les événements générés par
	//son application.
	public void run()
	{
		while(true) {
			//Offrir une petite pause à l'application; un système à événement n'a pas besoin
			//de réactions immédiates
			try {
				//Offrir une pause au thread
				Thread.sleep(1000);
				
				synchronized(lstEventsToSend) {
					if(lstEventsToSend.size() > 0) {
						IEvent ie = lstEventsToSend.get(0);
						System.out.println("Envoie de l'événement " + ie.toString());
						oos.writeObject(ie);
						lstEventsToSend.remove(0);
					}
				}

			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Compare un événement avec la liste d'événements à écouter.
	 */
	@SuppressWarnings("unchecked")
	public boolean listensToEvent(Object o)
	{
		boolean listens = false;
		for(int i = 0; i < listenedEvents.size() && !listens; i++)
		{
			Class c = listenedEvents.get(i);
			listens = (c.isInstance(o));
		}
		System.out.println("Réception de l'événement " + o.toString() + (listens?" traité":"ignoré"));
		return listens;
	}
	
	public void callEvent(IEvent ie) {
		lstEventsToSend.add(ie);
	}
	
	public void notifyObservers(IEvent event)
	{
		for(IObserver o : lstObserver) {
			o.update(this, event);
		}
	}
	
	public void addObserver(IObserver o) 
	{
		lstObserver.add(o);
	}
}

//Thread qui écoute les événements provenant du bus d'événements.
//Le Connector achemine les événements qui correspondent aux types à écouter
//dans listenedEvenets.
class ReadEventFromStream extends Thread {
	private ObjectInputStream ois;
	private EventBusConnector eventBusConn;
	public ReadEventFromStream(ObjectInputStream ois, EventBusConnector eventBusConn) {
		this.ois = ois;
		this.eventBusConn = eventBusConn;
	}
	
	public void run() {
		while(true) {
			try {
				Object o = ois.readObject();
				// Les événements reçus qui ne correspondent pas à ces types sont ignorés par
				// le Connector.
				if (eventBusConn.listensToEvent(o))
					eventBusConn.notifyObservers((IEvent)o);	
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
