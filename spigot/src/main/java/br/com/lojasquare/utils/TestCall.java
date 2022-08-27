package br.com.lojasquare.utils;

import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.providers.lojasquare.impl.LSProviderImpl;
import br.com.lojasquare.providers.request.IRequestProvider;
import br.com.lojasquare.providers.request.impl.RequestProviderImpl;
import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;
import lombok.Getter;

import java.util.List;

public class TestCall {

	@Getter private static ILSProvider lsProvider;
	@Getter private static IRequestProvider requestProvider;

	public static void main(String[] args) {
		SiteUtil su = new SiteUtil();
		su.setCredencial("ztNYLO1xKYGbZXFhfxHqnepQV2vSA5");
		su.setTokenServidor("tGBOxGZwo1wQ5LksKvwH");
		su.setConnectionTimeout(20000);
		su.setReadTimeout(20000);
		su.setServerRequest("https://162c-2804-1b3-3080-99b0-3c6c-84c8-1446-c2c4.ngrok.io");
		su.setDebug(true);
		requestProvider = new RequestProviderImpl(su);
		lsProvider = new LSProviderImpl(requestProvider, null);
		// buscaEntregas(eu);
		// buscaProdutosLoja(eu);
		validaIPs(su);
	}// */


	private static void validaIPs(SiteUtil su) {
		print(lsProvider.getIpMaquina().toString());
		//su.get("/v1/sites/extensoes");
		//print(su.get("/v1/sites/extensoes?showIP=true"));
	}


	private static void buscaProdutosLoja() {
		List<ProdutoInfo> pi 	= lsProvider.getTodosProdutosDaLoja();
		for(ProdutoInfo p : pi) {
			print(p.toString());
		}
	}


	private static void buscaEntregas() {
		List<ItemInfo> li = lsProvider.getTodasEntregas(LSEntregaStatus.PENDENTE);// getEntregasPlayer("Trow_Games"); if(li.size()>0){
		if(li == null || li.size() == 0) {
			print("OPA!");
			return;
		}
		for (ItemInfo ii : li) {
			print(ii.toString());
		}
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
