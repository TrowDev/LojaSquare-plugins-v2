package br.com.lojasquare.utils;

public class SiteUtil {

	private int connectionTimeout;
	private int readTimeout;
	private String credencial;
	private String ipMaquina;
	private String tokenServidor;
	private String serverRequest;
	private boolean debug;

	public SiteUtil() {
		connectionTimeout = 1500;
		readTimeout = 3000;
		debug = false;
	}

	public String getCredencial() {
		return credencial;
	}

	public void setCredencial(String keyAPI) {
		credencial = keyAPI;
	}

	public void setConnectionTimeout(int milisec) {
		connectionTimeout = milisec;
	}

	public void setReadTimeout(int milisec) {
		readTimeout = milisec;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean canDebug() {
		return debug;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setIpMaquina(String ipMaquina) {
		this.ipMaquina = ipMaquina;
	}

	public String getTokenServidor() {
		return tokenServidor;
	}

	public void setTokenServidor(String tokenServidor) {
		this.tokenServidor = tokenServidor;
	}

	public String getServerRequest() {
		return serverRequest;
	}

	public void setServerRequest(String serverRequest) {
		this.serverRequest = serverRequest;
	}
}
