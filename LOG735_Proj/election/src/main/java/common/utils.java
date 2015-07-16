package common;

import java.io.BufferedReader;
import java.io.IOException;

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
				break;
			} catch (IOException e) {
				System.out.println(failquestion);
				continue;
			}
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
		if(info.length != 2)
			return null; //not enough info
		String hostname = info[0];
		int port = Integer.parseInt(info[1]);
		return new ConnexionInfo(hostname, port);
	}
}
