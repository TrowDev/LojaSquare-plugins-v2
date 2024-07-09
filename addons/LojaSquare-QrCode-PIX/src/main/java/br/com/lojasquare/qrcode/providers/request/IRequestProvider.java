package br.com.lojasquare.qrcode.providers.request;

import br.com.lojasquare.qrcode.utils.HttpResponse;

import java.io.IOException;

public interface IRequestProvider {

    HttpResponse get(String endpoint) throws IOException;

    HttpResponse post(String endpoint, String body) throws IOException;

}
