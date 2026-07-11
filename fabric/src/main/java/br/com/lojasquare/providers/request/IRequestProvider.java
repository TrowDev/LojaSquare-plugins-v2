package br.com.lojasquare.providers.request;

import java.io.IOException;

import br.com.lojasquare.utils.HttpResponse;

public interface IRequestProvider {
    HttpResponse get(String url) throws IOException;
    HttpResponse put(String url) throws IOException;
}
