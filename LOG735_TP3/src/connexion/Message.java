package connexion;

import java.io.BufferedInputStream;
import java.io.IOException;

public class Message {

	
	private String message;

	public Message(String mess) {
		this.message = mess;
	}
	
	public String getMessageContent() {
		return message;
	}
	
	public byte[] getBytes() {
		String messToTransfert = "{" + this.message + "}";
		return messToTransfert.getBytes();
	}
	
	/**
	 * Retrieve message from BufferedInputStream
	 * @param inputStream
	 * @return new Message or null if an error occurs
	 */
	public static Message ParseMessage(BufferedInputStream inputStream)
	{
		if(inputStream == null)
			return null;
		
		try {
			char c = (char) inputStream.read();
			if(c == '{')
			{
				String m = "";
				while((c = (char) inputStream.read()) != '}')
				{
					m += c;
				}
				
				return new Message(m);
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
