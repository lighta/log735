package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import serverAccess.ConnexionInfo;

public class utils {
	/**
	 * Get input for a valid hostname:port
	 * retry till a valid ConnexionInfo is found
	 * @param in : Buffer to read data from
	 * @param failquestion : String to display when failing before reask
	 * @return
	 */
	public static ConnexionInfo readAddress(final BufferedReader in,String delimiter, final String failquestion){
		ConnexionInfo coninfo = null;
		while(coninfo==null){
			try {
				String hostname = in.readLine();
				coninfo = parseBindAddress(hostname,delimiter);
			} catch (IOException e) {
				continue;
			}
			if(coninfo==null) System.out.println(failquestion);
		}
		return coninfo;
	}
	
	/**
	 * Lis une entree host:port, et cree une nouvelle ConnexionInfo avec
	 * @param s : String a lire
	 * @return ConnexionInfo or null
	 */
	public static ConnexionInfo parseBindAddress(String s, String delimiter) {
		String[] info = s.split(delimiter);
		int id = -1;
		int port;
		String hostname;
		if(info.length < 2 || info[0].isEmpty() || info[1].isEmpty())
			return null; //not enough info
		
		if(info.length == 3)
			try {
				id = Integer.parseInt(info[2]);
			} catch (NumberFormatException e) {
				System.out.println("Bad input number for port");
				return null;
			}
		
		try {
            InetAddress.getByName(info[0]);  //perhaps save as Inet
            hostname = info[0];
        } catch (UnknownHostException ex) {
        	System.out.println("Bad input for hostname");
        	return null;
        }
		try {
			port = Integer.parseInt(info[1]);
		} catch (NumberFormatException e) {
			System.out.println("Bad input number for port");
			return null;
		}
		
		ConnexionInfo cInfo = new ConnexionInfo(hostname, port);
		cInfo.setId(id);
		
		return cInfo;
		
	}
}
