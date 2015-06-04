package events;
/******************************************************
 Cours : LOG735
 Session : Été 2015
 Groupe : 01
 Projet : Laboratoire #N
 Étudiants :	Charly Simon
		Max Moreau
 Code(s) perm.:	SIMC28069108
		MORM30038905
 Date création : 02 Juin 2015
 Date dern. modif. : 02 juin 2015
******************************************************
Definit un nouveau type d'evenement
******************************************************/
public class EventAckFin extends EventForAll implements IEventAckFin {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1156382431931313979L;

	public EventAckFin(String m){
		super(m);
	}
}
