package serverAccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;



public class Commande {

	private final static String DELIMITER_CHARACTER_START = "!";
	private final static String DELIMITER_CHARACTER_END = "";
	
	private static Logger log = Logger.getLogger(Commande.class);
		
	public enum CommandeType 
	{
		/**
		 * 
		 */
		RESTART,
		/**
		 * 
		 */
		STOP,
		/**
		 * 
		 */
		STATE,
		/**
		 * 	
		 */
		HELLO,
		/**
		 * 
		 */
		ALIVE
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
	
	byte[] getBytes() {
		String messToTransfert = DELIMITER_CHARACTER_START+type.name() + "#" + this.message + DELIMITER_CHARACTER_END + "\n";
		return messToTransfert.getBytes();
	}
	
	/**
	 * Retrieve message from BufferedInputStream
	 * @param inputStream
	 * @return new Message or null if an error occurs
	 * @throws IOException 
	 */
	static Commande ParseCommande(InputStream inputStream) throws IOException
	{
		log.debug("Try to parse commande" );
		
		if(inputStream == null)
		{
			log.debug("Null input Stream" );
			return null;
		}
		
		if(inputStream.available() < 1){
			return null;
		}
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			
			
			String comm = "";
			String content = "";
			
			String rec;
			log.debug("Reading ..." );
			while ((rec = in.readLine()) != null){
				if(rec.startsWith(DELIMITER_CHARACTER_START) 
						//&& rec.endsWith(DELIMITER_CHARACTER_END)
						)
				{
					
					log.debug("Commande receive !!! " + rec );
					final String part[] = rec.substring(1, 
							rec.length()//-2)
							).split("#");
					
					if(part.length < 1){
						System.out.println("Malformed packet received");
						continue;
					}
					
					comm = part[0];
					
					log.debug("Commande type separator found : " + comm );
					
					CommandeType ctype = CommandeType.valueOf(comm);
					
					if(ctype == null){
						log.debug("Unknown Commande Type : " + comm );
						return null;
					}
					log.debug("Commande type receive : " + ctype );
					
					if(part.length > 1)
					{
						content += part[1];
					}
					
					log.debug("Commande content receive : " + content );
					
					return new Commande(ctype,content);
				}
				
				log.debug("Not reconized as commade");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
	
		return null;
	}
	
	@Override
	public String toString() {
		return "" + DELIMITER_CHARACTER_START + this.type + "#" + this.message + DELIMITER_CHARACTER_END;
	}

	public CommandeType getType() {
		return this.type;
	}
	
}
