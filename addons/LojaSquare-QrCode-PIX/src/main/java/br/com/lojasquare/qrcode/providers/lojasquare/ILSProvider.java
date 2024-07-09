package br.com.lojasquare.qrcode.providers.lojasquare;

import br.com.lojasquare.qrcode.utils.model.Checkout;
import br.com.lojasquare.qrcode.utils.model.CheckoutResponse;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfo;

import java.util.List;

public interface ILSProvider {
    List<ProdutoInfo> getTodosProdutosDaLoja(String tokenServidor);
    CheckoutResponse getQrCodePayment(Checkout checkout);
}
