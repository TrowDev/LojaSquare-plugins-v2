package br.com.lojasquare.utils;

import lombok.Data;

@Data
public class SiteUtil {

	private int connectionTimeout;
	private int readTimeout;
	private String credencial;
	private String chavePublica;
	private String ipMaquina;
	private String tokenServidor;
	private String serverRequest;
	private boolean debug;

	public SiteUtil() {
		connectionTimeout = 1500;
		readTimeout = 3000;
		debug = false;
	}
}
