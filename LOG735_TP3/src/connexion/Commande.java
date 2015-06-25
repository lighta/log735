package connexion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import logs.Logger;


public class Commande {

	private final static String DELIMITER_CHARACTER_START = "!";
	private final static String DELIMITER_CHARACTER_END = "";
	
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
		 * suc|bnk->suc, envoi la list des succursales
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
		 * suc->con, notification que le transfert est authoris� (commenc�)
		 */
		TFACK,
		/**
		 * suc->con, notification que le transfert est effectu�
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
		 * aquite que la succ a bien setter le montant
		 */
		SETMACK,
		/**
		 * con->bnk, demande de creation de connexion entre succursale et banque
		 */
		CRCON,
		/**
		 * suc|bnk->con, envoie la liste des succursale a la console
		 */
		NLIST,
		/**
		 * suc->bnk, Request Succ list
		 */
		GETLIST,
		/**
		 * bnk->suc, notify new succ registered
		 */
		ADDLIST,
		/**
		 * 	bnk->con send amountofnewsucc:totalamount
		 */
		HAM,
		/**
		 * 	con->suc authentificate console for succursale
		 */
		HELLO,
		/**
		 * 	con->suc|bnk get the list recorded on that node
		 */
		SHOWLIST,
		/**
		 * 	con->suc|bnk get the state of the node
		 */
		SHOWSTATE,
		/**
		 * 	suc->bnk recupere le montant total de la banque
		 */
		GETTOTAL,
		/**
		 * 	bnk->suc transmet le montant total
		 */
		TOTAL
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
		String messToTransfert = DELIMITER_CHARACTER_START+type.name() + "#" + this.message + DELIMITER_CHARACTER_END + "\n";
		return messToTransfert.getBytes();
	}
		
	/**
	 * Retrieve message from BufferedInputStream
	 * @param inputStream
	 * @return new Message or null if an error occurs
	 * @throws IOException 
	 */
	public static Commande ParseCommande(InputStream inputStream) throws IOException
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
			
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

			String comm = "";
			String content = "";
			
			String rec;
			log.message("Reading ..." );
			while ((rec = in.readLine()) != null){
				if(rec.startsWith(DELIMITER_CHARACTER_START) 
						//&& rec.endsWith(DELIMITER_CHARACTER_END)
						)
				{
					
					log.message("Commande receive !!! " + rec );
					final String part[] = rec.substring(1, 
							rec.length()//-2)
							).split("#");
					
					if(part.length < 1){
						System.out.println("Malformed packet received");
						continue;
					}
					
					comm = part[0];
					
					log.message("Commande type separator found : " + comm );
					
					CommandeType ctype = CommandeType.valueOf(comm);
					
					if(ctype == null){
						log.message("Unknown Commande Type : " + comm );
						return null;
					}
					log.message("Commande type receive : " + ctype );
					
					if(part.length > 1)
					{
						content += part[1];
					}
					
					log.message("Commande content receive : " + content );
					
					return new Commande(ctype,content);
				}
				
				log.message("Not reconized as commade");
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
