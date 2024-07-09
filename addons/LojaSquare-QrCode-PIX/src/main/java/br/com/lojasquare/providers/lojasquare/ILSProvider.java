package br.com.lojasquare.providers.lojasquare;

import br.com.lojasquare.utils.model.Checkout;
import br.com.lojasquare.utils.model.CheckoutResponse;
import br.com.lojasquare.utils.model.ProdutoInfo;

import java.util.List;

public interface ILSProvider {
    List<ProdutoInfo> getTodosProdutosDaLoja(String tokenServidor);
    CheckoutResponse getQrCodePayment(Checkout checkout);
}
