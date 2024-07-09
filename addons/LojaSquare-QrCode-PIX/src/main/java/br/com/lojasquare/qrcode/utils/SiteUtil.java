package br.com.lojasquare.qrcode.utils;

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
		connectionTimeout = 5000;
		readTimeout = 10000;
		debug = false;
	}
}
