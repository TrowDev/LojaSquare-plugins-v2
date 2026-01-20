package br.com.lojasquare.core;

/**
 * Interface para serviços de checagem executados na inicialização do plugin.
 * Equivalente ao CheckService do Spigot.
 */
public interface CheckService {

    /**
     * Executa a checagem/inicialização do serviço.
     */
    void execute();
}
