package br.com.lojasquare.utils;

import lombok.Data;

/**
 * Configurações de conexão com a API LojaSquare.
 * Classe agnóstica de plataforma - adaptada do projeto Spigot.
 */
@Data
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
        serverRequest = "https://api.lojasquare.net";
    }

    public boolean canDebug() {
        return debug;
    }
}
