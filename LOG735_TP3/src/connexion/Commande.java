package connexion;

import java.io.BufferedInputStream;
import java.io.IOException;

import logs.Logger;


public class Commande {

	private final static char DELIMITER_CHARACTER_START = '!';
	private final static char DELIMITER_CHARACTER_END = '!';
	
	private static final Logger log = Logger.createLog(Commande.class);
	
	public enum CommandeType
	{
		/**
		 * suc->suc, receive connexion request
		 */
		TUN,
		/**
		 * bnk->suc, give succ bank
		 */
		ID,
		/**
		 * 
		 */
		STATE,
		/**
		 * bnk->suc, envoi la list des succursales
		 */
		LIST,	
		/**
		 * /all->all, discuss
		 */
		MESS,	
		/** 
		 * con->suc, demande de transfert par console
		 */
		TFCON,
		/**
		 * suc->suc, demande de transfert par succursale
		 */
		TFSUC,
		/**
		 * suc->con, notification que le transfert est authorisé (commencé)
		 */
		TFACK,
		/**
		 * suc->con, notification que le transfert est effectué
		 */
		TFDONE,
		/**
		 * con->suc, demande d'une creation de connexion de la succursale a la banque
		 */
		CON,
		/**
		 * suc->con  notification que la connection s'est bien faite
		 */
		CONACK,
		/**
		 * con->suc|bnk  con->bnk, demande de creation d'un bug
		 */
		BUG,
		/**
		 * suc|bnk->con, notification sur l'acceptation de la creation de bug
		 */
		BUGACK,
		/**
		 * suc->bnk, demande d'enregistrement de la succursale a la banque
		 */
		REG,
		/**
		 * con->suc, maj du montant d'une succursale, (devrait etre fait avant REG)
		 */
		SETM,
		/**
		 * con->bnk, demande de creation de connexion entre succursale et banque
		 */
		CRCON,
		/**
		 * suc->con, envoie la liste des succursale a la console
		 */
		NLIST
		
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
		String messToTransfert = DELIMITER_CHARACTER_START+type.name() + "#" + this.message + DELIMITER_CHARACTER_END;
		return messToTransfert.getBytes();
	}
		
	/**
	 * Retrieve message from BufferedInputStream
	 * @param inputStream
	 * @return new Message or null if an error occurs
	 * @throws IOException 
	 */
	public static Commande ParseCommande(BufferedInputStream inputStream) throws IOException
	{
		log.message("Try to parse commande" );
		
		if(inputStream == null)
		{
			log.message("Null input Stream" );
			return null;
		}
		
		if(inputStream.available() < 1){
			return null;
		}
		
		try {
			log.message("Parse first char" );
			Character c = (char) inputStream.read();
			if(c == DELIMITER_CHARACTER_START)
			{
				
				String comm = "";
				String content = "";
				
				log.message("Commande receive !!! ( parsing Type )" );
				
				while((c = (char) inputStream.read()) != '#')
				{
					comm += c;
				}
				
				log.message("Commande type separator found : " + comm );
				
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
				{
					log.message("Unknown Commande Type" );
					return null;
				}
				log.message("Commande type receive : " + ctype );

				while((c = (char) inputStream.read()) != DELIMITER_CHARACTER_END)
				{
					content += c;
				}
				
				log.message("Commande content receive : " + content );
				
				return new Commande(ctype,content);
			}
			
			log.message("Not reconized as commade: First char = " + c);
			
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
