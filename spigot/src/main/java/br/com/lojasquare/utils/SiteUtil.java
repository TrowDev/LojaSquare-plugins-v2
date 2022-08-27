package br.com.lojasquare.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;

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
	
	public boolean validacao() {
		int i = 0;
		if(getReadTimeout() <= 0) {
			i = -1;
		} else if(getConnectionTimeout() <= 0) {
			i = -2;
		} else if(getCredencial() == null || getCredencial().equals("")) {
			i = -3;
		} else if(getTokenServidor() == null || getTokenServidor().equals("")) {
			i = -4;
		} else if(getServerRequest() == null || getServerRequest().equals("")) {
			i = -5;
		}
		return i == 0;
	}

	/**
	 * Este metodo faz a requisicao na api, no endpoint especificado.
	 * 
	 * @param endpoint - Caminho onde a chamada deve ser realizada.
	 * @return Retorna o JSON, caso tenha algum registro a ser retornado.
	 */
	public String get(final String endpoint) {
		if(!validacao()) {
			return "LS-[LojaSquare] Validacao antes de requisicao nao atendida.";
		}
		HttpURLConnection c = null;
		int statusCode = 0;
		try {
			final StringBuilder sb2 = new StringBuilder();
			final URL u = new URL(sb2.append(getServerRequest()).append(endpoint).toString());
			c = (HttpURLConnection) u.openConnection();
			c.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0) lojasquare");
			c.setRequestMethod("GET");
			c.setRequestProperty("Authorization", getCredencial());
			c.setRequestProperty("Content-Type", "application/json");
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			c.setConnectTimeout(getConnectionTimeout());
			c.setReadTimeout(getReadTimeout());
			c.connect();
			statusCode = c.getResponseCode();
//			print("Status Code From "+endpoint+" : "+statusCode);
			if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
				final BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
				final StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();
				return sb.toString();
			}
		} catch (IOException ex) {
			if (canDebug()) {
				ex.printStackTrace();
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
			} else {
				msgConsole("§4[LojaSquare] §cErro ao tentar conexao com o site. Erro: §a" + ex.getMessage());
			}
			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception ex2) {
					if (canDebug()) {
						ex2.printStackTrace();
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex2);
					} else {
						msgConsole("§4[LojaSquare] §cErro ao fechar a conexao com o site. Erro: §a" + ex2.getMessage());
					}
				}
			}
		} finally {
			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception ex3) {
					if (canDebug()) {
						ex3.printStackTrace();
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex3);
					} else {
						msgConsole("§4[LojaSquare] §cErro ao fechar a conexao com o site. Erro: §a" + ex3.getMessage());
					}
				}
			}
		}
		return "LS-" + getResponseByCode(statusCode);
	}

	/**
	 * Este metodo faz a requisicao de update na api.
	 * 
	 * @param endpoint - Caminho onde a requisicao deve ser realizada.
	 * @return Retorna true, caso o update seja realizado com sucesso.
	 */
	public boolean update(final String endpoint) {
		if(!validacao()) {
			return false;
		}
		HttpsURLConnection c = null;
		int statusCode = 0;
		try {
			final StringBuilder sb = new StringBuilder();
			final URL u = new URL(sb.append(getServerRequest()).append(endpoint).toString());
			c = (HttpsURLConnection) u.openConnection();
			c.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0) lojasquare");
			c.setRequestMethod("PUT");
			c.setRequestProperty("Authorization", getCredencial());
			c.setRequestProperty("Content-Type", "application/json");
			c.setDoOutput(true);
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			c.setConnectTimeout(getConnectionTimeout());
			c.setReadTimeout(getReadTimeout());
			c.connect();
			statusCode = c.getResponseCode();
			if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
				return true;
			}
		} catch (IOException ex) {
			if (canDebug()) {
				ex.printStackTrace();
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
			} else {
				msgConsole("§4[LojaSquare] §cErro ao tentar conexao com o site. Erro: §a" + ex.getMessage());
			}
			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception ex2) {
					if (canDebug()) {
						ex2.printStackTrace();
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex2);
					} else {
						msgConsole("§4[LojaSquare] §cErro ao fechar a conexao com o site. Erro: §a" + ex2.getMessage());
					}
				}
			}
		} finally {
			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception ex3) {
					if (canDebug()) {
						ex3.printStackTrace();
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex3);
					} else {
						msgConsole("§4[LojaSquare] §cErro ao fechar a conexao com o site. Erro: §a" + ex3.getMessage());
					}
				}
			}
		}
		print(getResponseByCode(statusCode));
		return false;
	}

	public String getResponseByCode(int i) {
		String msg = "";
		switch (i) {
		case 0:
			msg = "[LojaSquare-v2] §cServidor sem conexao com a internet.";
			break;
		case 401:
			msg = "[LojaSquare-v2] §cConexao nao autorizada! Por favor, confira se a sua credencial esta correta.";
			break;
		case 403:
			msg = "[LojaSquare-v2] §cO IP da maquina do seu servidor ou a sua key-api foram bloqueados.";
			break;
		case 404:
			msg = "[LojaSquare-v2] §cNao foi encontrado nenhum registro para a requisicao efetuada.";
			break;
		case 405:
			msg = "[LojaSquare-v2] §cErro ao autenticar sua loja! Verifique se sua assinatura e credencial estao ativas!";
			break;
		case 406:
			msg = "[LojaSquare-v2] §cNao foi executada nenhuma atualizacao referente ao requerimento efetuado.";
			break;
		case 409:
			msg = "[LojaSquare-v2] §cO IP enviado e diferente do que temos em nosso Banco de Dados. IP da sua Maquina: §a"
					+ getIpMaquina();
			break;
		default:
			msg = "[LojaSquare-v2] §cProvavel falha causada por entrada de dados incompativeis com o requerimento efetuado. Status Code: "
					+ i;
			break;
		}
		return msg;
	}

	public static int parseInt(String a) {
		return Integer.parseInt(a);
	}

	public static void print(String a) {
		System.out.println(a);
	}

	public static void print(int a) {
		System.out.println(a);
	}

	public static void print(double a) {
		System.out.println(a);
	}

	public void msgConsole(String s) {
		Bukkit.getConsoleSender().sendMessage(s);
	}

	public String getIpMaquina() {
		if (ipMaquina == null) {
			String getIP = this.get("/v1/sites/extensoes?showIP=true");
			// checagem criada para evitar erros de repeti§§o como este:
			// https://prnt.sc/vql2p3
			if (getIP.length() > 20) {
				ipMaquina = "n§o identificado.";
			} else {
				ipMaquina = getIP;
			}
		}
		return ipMaquina;
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
