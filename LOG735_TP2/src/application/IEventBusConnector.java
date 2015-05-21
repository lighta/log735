/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Interface qui sert de Facade pour EventBusConnector.
******************************************************/ 
package application;
import events.*;

public interface IEventBusConnector {
	void callEvent(IEvent ie);
}
