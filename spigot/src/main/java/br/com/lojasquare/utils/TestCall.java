package br.com.lojasquare.utils;

import java.util.List;

import br.com.lojasquare.LojaSquare;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.providers.lojasquare.impl.LSProviderImpl;
import br.com.lojasquare.providers.request.IRequestProvider;
import br.com.lojasquare.providers.request.impl.RequestProviderImpl;
import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;
import lombok.Getter;

public class TestCall {

	@Getter private static ILSProvider lsProvider;
	@Getter private static IRequestProvider requestProvider;

	public static void main(String[] args) {
		SiteUtil su = new SiteUtil();
		su.setCredencial("ztNYLO1xKYGbZXFhfxHqnepQV2vSA5");
		su.setTokenServidor("Fey2yWTHEhLts4YcJ9Mq");
		su.setConnectionTimeout(20000);
		su.setReadTimeout(20000);
		su.setServerRequest("https://api.lojasquare.net");
		su.setDebug(true);
		requestProvider = new RequestProviderImpl(su);
		lsProvider = new LSProviderImpl(requestProvider, LojaSquare.getInstance());
		// buscaEntregas(eu);
		// buscaProdutosLoja(eu);
		validaIPs(su);
	}// */


	private static void validaIPs(SiteUtil su) {
		su.get("/v1/sites/extensoes");
		print(su.get("/v1/sites/extensoes?showIP=true"));
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
