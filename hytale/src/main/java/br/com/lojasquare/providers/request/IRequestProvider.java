package br.com.lojasquare.providers.request;

import java.io.IOException;

import br.com.lojasquare.utils.HttpResponse;

/**
 * Interface para provider de requisições HTTP.
 * Classe agnóstica de plataforma - reutilizada do projeto Spigot.
 */
public interface IRequestProvider {

    HttpResponse get(String url) throws IOException;

    HttpResponse put(String url) throws IOException;
}
