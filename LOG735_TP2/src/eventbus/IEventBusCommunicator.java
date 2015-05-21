/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Interface pour EventBusCommunicator.
******************************************************/ 
package eventbus;

import events.IEvent;

public interface IEventBusCommunicator {
	public void sendToListener(IEvent ie);
}
