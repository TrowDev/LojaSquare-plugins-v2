package me.trow.lojasquare.utils;

import java.util.List;

import me.trow.lojasquare.utils.model.ItemInfo;
import me.trow.lojasquare.utils.model.ProdutoInfo;

public class TestCall {


	public static void main(String[] args) {
		SiteUtil su = new SiteUtil();
		su.setCredencial("ztNYLO1xKYGbZXFhfxHqnepQV2vSA5");
		su.setTokenServidor("Fey2yWTHEhLts4YcJ9Mq");
		su.setConnectionTimeout(20000);
		su.setReadTimeout(20000);
		su.setServerRequest("https://api.lojasquare.net");
		su.setDebug(true);
		ExecutorUtil eu = new ExecutorUtil(su);
		// buscaEntregas(eu);
		// buscaProdutosLoja(eu);
		validaIPs(su);
	}// */


	private static void validaIPs(SiteUtil su) {
		su.get("/v1/sites/extensoes");
		print(su.get("/v1/sites/extensoes?showIP=true"));
	}


	private static void buscaProdutosLoja(ExecutorUtil eu) {
		List<ProdutoInfo> pi 	= eu.getTodosProdutosDaLoja();
		for(ProdutoInfo p : pi) {
			print(p.toString());
		}
	}


	private static void buscaEntregas(ExecutorUtil eu) {
		List<ItemInfo> li = eu.getTodasEntregas("1");// getEntregasPlayer("Trow_Games"); if(li.size()>0){
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
