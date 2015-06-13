package connexion;

import java.io.BufferedInputStream;
import java.io.IOException;


public class Commande {

	public enum CommandeType
	{
		TUN,
		ID,
		STATE,
		LIST,
		MESS
	}
	
	
	private CommandeType type;
	private String message;

	public Commande(CommandeType type, String mess) {
		this.type = type;
		this.message = mess;
	}
	
	public String getMessageContent() {
		return message;
	}
	
	public byte[] getBytes() {
		String messToTransfert = "{"+type.name() + "#" + this.message + "}";
		return messToTransfert.getBytes();
	}
		
	/**
	 * Retrieve message from BufferedInputStream
	 * @param inputStream
	 * @return new Message or null if an error occurs
	 */
	public static Commande ParseCommande(BufferedInputStream inputStream)
	{
		if(inputStream == null)
			return null;
		
		try {
			char c = (char) inputStream.read();
			if(c == '{')
			{
				String comm = "";
				String content = "";
				while((c = (char) inputStream.read()) != '#')
				{
					comm += c;
				}
				
				CommandeType ctype;
				
				if( CommandeType.TUN.name().equals(comm))
					ctype =  CommandeType.TUN;
				else if( CommandeType.ID.name().equals(comm))
					ctype =  CommandeType.ID;
				else if( CommandeType.STATE.name().equals(comm))
					ctype =  CommandeType.STATE;
				else if( CommandeType.LIST.name().equals(comm))
					ctype =  CommandeType.LIST;
				else if( CommandeType.MESS.name().equals(comm))
					ctype =  CommandeType.MESS;
				else
					return null;
				

				while((c = (char) inputStream.read()) != '}')
				{
					content += c;
				}
				
				return new Commande(ctype,content);
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
