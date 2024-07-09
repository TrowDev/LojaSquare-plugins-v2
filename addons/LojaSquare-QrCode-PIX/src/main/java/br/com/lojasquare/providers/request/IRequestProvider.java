package br.com.lojasquare.providers.request;

import br.com.lojasquare.utils.HttpResponse;

import java.io.IOException;

public interface IRequestProvider {

    HttpResponse get(String endpoint) throws IOException;

    HttpResponse post(String endpoint, String body) throws IOException;

}
