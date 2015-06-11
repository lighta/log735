package connexion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import succursale.SuccursalesInfo;

public class Tunnel {
	private final BufferedOutputStream out;
	private final BufferedInputStream in;
	private final Socket socket;

	public Tunnel(ConnexionInfo s2) throws IOException {
		super();
		socket = new Socket(s2.getHostname(), s2.getPort());
		out = new BufferedOutputStream(socket.getOutputStream());
		in = new BufferedInputStream(socket.getInputStream());
	}
	
	public BufferedOutputStream getOut() {
		return out;
	}
	public BufferedInputStream getIn() {
		return in;
	}


	public void destroy(){ //clean up all ressource
		try {
			if (out != null)
				out.close();
		} catch (Exception e) {
			// On ignore
		}
		try {
			if (in != null)
				in.close();
		} catch (Exception e) {
			// On ignore
		}
		try {
			if (socket != null)
				socket.close();
		} catch (Exception e) {
			// On ignore
		}
		
	}

	public void askStatus() {
		String req = "Give me your status !";
		try {
			out.write(req.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
