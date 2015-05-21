/******************************************************
	Cours :           LOG730
	Session :         Été 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date création :   2010-05-21
******************************************************
Interface Observer pour le patron Observer utilisé dans
l'application.
******************************************************/ 
package application;

public interface IObserver {
	void update(Object observable, Object arg);
}
