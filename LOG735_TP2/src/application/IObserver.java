/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
Interface Observer pour le patron Observer utilis� dans
l'application.
******************************************************/ 
package application;

public interface IObserver {
	void update(Object observable, Object arg);
}
