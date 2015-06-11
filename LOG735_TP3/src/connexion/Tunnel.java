package connexion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import succursale.SuccursalesInfo;

public class Tunnel {
	private final BufferedOutputStream out;
	private final BufferedInputStream in;
	private final Socket socket;

	//constructeur pour emetteur
	public Tunnel(SuccursalesInfo s1, SuccursalesInfo s2) throws IOException {
		super();
		this.socket = new Socket(s2.getHostname(), s2.getPort());
		out = new BufferedOutputStream(socket.getOutputStream());
		in = new BufferedInputStream(socket.getInputStream());
		
		askTunnel(s1);
	}
	
	//constructeur pour receiver
	public Tunnel(Socket socket) throws IOException {
		super();
		this.socket = socket;
		out = new BufferedOutputStream(socket.getOutputStream());
		in = new BufferedInputStream(socket.getInputStream());
	}
	
	public BufferedOutputStream getOut() {
		return out;
	}
	public BufferedInputStream getIn() {
		return in;
	}
	public Socket getSocket() {
		return socket;
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

	private void askTunnel(SuccursalesInfo s1) {
		String req = "TUN#"+s1.getId();
		try {
			out.write(req.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void askStatus() {
		String req = "REQ_STATUS#";
		try {
			out.write(req.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
