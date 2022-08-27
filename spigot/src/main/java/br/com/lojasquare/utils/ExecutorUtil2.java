package br.com.lojasquare.utils;

import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class ExecutorUtil2 {

	private SiteUtil siteUtil;

	public ExecutorUtil2(SiteUtil siteUtil) {
		super();
		this.siteUtil = siteUtil;
	}

	/**
	 * Retorna uma lista de produtos a serem entregues (Retorno para todos os
	 * players).
	 * 
	 * @return List<ItemInfo> lista de produtos a serem entregues.
	 */
	public List<ItemInfo> getTodasEntregas(String status) {
		List<ItemInfo> itens = new ArrayList<>();
		try {
			String result = getSiteUtil().get(String.format("/v1/entregas/%s", status));
			//print(result);
			if (result.startsWith("LS-"))
				return itens;
			JsonArray job = new JsonParser().parse(result).getAsJsonArray();
			for (int i = 0; i < job.size(); i++) {
				try {
					ItemInfo ii = new Gson().fromJson(job.get(i), ItemInfo.class);
					itens.add(ii);
				} catch (Exception e) {
					print("[LojaSquare] Nao foi possivel processar o item " + job.get(i).toString()
							+ ". Erro: " + e.getMessage());
				}
			}
			return itens;
		} catch (Exception e) {
			e.printStackTrace();
			return itens;
		}
	}
	
	/**
	 * Busca todos os produtos criados na loja e seus respectivos grupos
	 * @return
	 */
	public List<ProdutoInfo> getTodosProdutosDaLoja() {
		List<ProdutoInfo> prods = new ArrayList<>();
		try {
			String result = getSiteUtil().get(String.format("/v1/produtos?tokenSubServidor=%s", getSiteUtil().getTokenServidor()));
			if (result.startsWith("LS-"))
				return prods;
			JsonArray job = new JsonParser().parse(result).getAsJsonArray();
			for (int i = 0; i < job.size(); i++) {
				try {
					ProdutoInfo ii = new Gson().fromJson(job.get(i), ProdutoInfo.class);
					prods.add(ii);
				} catch (Exception e) {
					print("[LojaSquare] Nao foi possivel processar o produto " + job.get(i).toString()
							+ ". Erro: " + e.getMessage());
				}
			}
			return prods;
		} catch (Exception e) { }
		return prods;
	}
	
	/**
	 * O metodo executa o update da entrega, informando na api que a entrega com ID
	 * 'x' foi realizada.
	 *
	 * @param ii ItemInfo a ser entregue e atualizado
	 * @return Retornara true se o update da entrega for realizado com sucesso.
	 */
	public boolean updateDelivery(ItemInfo ii) {
		if (ii == null) {
			return false;
		}
		return getSiteUtil().update(String.format("/v1/entregas/%d/entregue", ii.getEntregaID()));
	}

	/**
	 * Retorna uma lista de produtos a serem entregues para um player em especifico.
	 * 
	 * @param player - Nick do player, usado como referencia para filtrar as
	 *               entregas.
	 * @return List<ItemInfo> lista de produtos a serem entregues.
	 */
	public List<ItemInfo> getEntregasPlayer(String player) {
		List<ItemInfo> itens = new ArrayList<>();
		try {
			String result = getSiteUtil().get(String.format("/v1/queue/%s", player));
			if (result.startsWith("LS-")) {
				return itens;
			}
			JsonObject job = new JsonParser().parse(result).getAsJsonObject();
			for (int i = 1; i <= job.entrySet().size(); i++) {
				try {
					ItemInfo ii = new Gson().fromJson(job.getAsJsonObject(i + ""), ItemInfo.class);
					itens.add(ii);
				} catch (Exception e) {
					print("[LojaSquare] Nao foi possivel processar o item " + job.getAsJsonObject(i + "").toString()
							+ ". Erro: " + e.getMessage());
				}
			}
			return itens;
		} catch (Exception e) {
			return itens;
		}
	}

	
	public void print(String msg) {
		System.out.println(msg);
	}

	public SiteUtil getSiteUtil() {
		return siteUtil;
	}

	public void setSiteUtil(SiteUtil siteUtil) {
		this.siteUtil = siteUtil;
	}
	
}
