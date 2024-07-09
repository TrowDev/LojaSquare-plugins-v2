package br.com.lojasquare.utils;

import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.providers.lojasquare.impl.LSProviderImpl;
import br.com.lojasquare.providers.request.IRequestProvider;
import br.com.lojasquare.providers.request.impl.RequestProviderImpl;
import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.enums.LSGateway;
import br.com.lojasquare.utils.model.*;
import com.google.gson.Gson;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class TestCall {

	@Getter private static ILSProvider lsProvider;
	@Getter private static IRequestProvider requestProvider;

	public static void main(String[] args) {
		SiteUtil su = new SiteUtil();
		su.setCredencial("ztNYLO1xKYGbZXFhfxHqnepQV2vSA5");
		su.setChavePublica("spDLPRDvl2H9ExRLXdbG5rwJrKg4Nm");
		su.setTokenServidor("tGBOxGZwo1wQ5LksKvwH");
		su.setConnectionTimeout(20000);
		su.setReadTimeout(20000);
		su.setServerRequest("https://api.lojasquare.net");
		su.setDebug(true);
		requestProvider = new RequestProviderImpl(su);
		lsProvider = new LSProviderImpl(requestProvider, null, new Gson());
		buscaProdutosLoja();
//		geraQrCodeCheckout();
	}// */


	private static void buscaProdutosLoja() {
		List<ProdutoInfo> pi 	= lsProvider.getTodosProdutosDaLoja("tGBOxGZwo1wQ5LksKvwH");
		for(ProdutoInfo p : pi) {
			print(p.toString());
		}
	}

	private static void geraQrCodeCheckout() {
		CheckoutResponse checkoutResponse = lsProvider.getQrCodePayment(Checkout.builder()
						.carrinho(Collections.singletonList(ItemInfo.builder()
										.produtoId(178L).quantidade(2)
								.build()))
						.cliente(Cliente.builder().clienteID(0L).build())
						.gateway(LSGateway.PAGSQUARE.getGateway())
						.player("trow")
						.servidor("TrowCraft2")
				.build());
		print(checkoutResponse.isImg() ? "QR_CODE" : "LINK_PGMTO");
		print(checkoutResponse.isError() ? "FALHA" : "SUCESSO");
		print(checkoutResponse.getMessage());
		print(checkoutResponse.getCode());
		print(checkoutResponse.getUrlPayment());
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
}
