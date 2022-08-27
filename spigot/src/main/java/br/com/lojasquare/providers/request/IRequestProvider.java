package br.com.lojasquare.providers.request;

import br.com.lojasquare.utils.HttpResponse;

import java.io.IOException;

public interface IRequestProvider {

    HttpResponse get(String url) throws IOException;

    HttpResponse put(String url) throws IOException;

}
