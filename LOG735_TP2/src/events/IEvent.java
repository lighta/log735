/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Interface pour la classe Event.
******************************************************/ 
package events;

import java.io.Serializable;

public interface IEvent extends Serializable {
	public String getMessage();
}
