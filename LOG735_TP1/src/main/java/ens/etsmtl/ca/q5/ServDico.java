package ens.etsmtl.ca.q5;

import java.util.HashMap;
import java.util.Map;



public class ServDico {
	protected String host_name = "";
	protected int port = 10118;
	
	public class ServerDef {
		public String host_name = "";
		public int port = 10118;
		public ServerDef(String host_name, int port) {
			this(host_name);
			this.port = port;
		}
		public ServerDef(String host_name) {
			this.host_name = host_name;
		}
		
		@Override
		public String toString() {
			return "hostname="+host_name+" port="+port;
		}
	}
	
	public Map<Integer, ServerDef> servers_dico = new HashMap<>();


	public ServDico() {
		super();
		FillServerDico();
	}

	private void FillServerDico() {
		servers_dico.put(0, new ServerDef("127.0.0.1"));
		servers_dico.put(1, new ServerDef("127.0.0.2"));
	}
}
