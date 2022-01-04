package me.trow.lojasquare.core.autoconfig;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import me.trow.lojasquare.LojaSquare;
import me.trow.lojasquare.utils.ConfigManager;
import me.trow.lojasquare.utils.model.ProdutoInfo;

public class CheckCreateGroupItem {
	
	private static LojaSquare pl;
	
	public CheckCreateGroupItem(LojaSquare pl) {
		this.pl = pl;
	}
	
	public List<String> getListaGruposProdutosDaLoja() {
		List<ProdutoInfo> prods = pl.getExecutorUtil().getTodosProdutosDaLoja();
		List<String> grupos 	= new ArrayList<String>();
		if(prods != null) {
			for(ProdutoInfo pi : prods) {
				if(grupos.contains(pi.getGrupo())) continue;
				grupos.add(pi.getGrupo());
			}
		}
		return grupos;
	}
	
	public void checaGruposConfigurados() {
		List<String> gruposConfigPlugin 			= pl.getProdutosConfigurados();
		if(gruposConfigPlugin != null) {
			List<String> gruposDeProdutosNoSite 	= getListaGruposProdutosDaLoja();
			if(gruposDeProdutosNoSite != null) {
				for(String grupoSite : gruposDeProdutosNoSite) {
					if(gruposConfigPlugin.contains(grupoSite)) continue;
					criarConfiguracaoDoProduto(grupoSite);
				}
			}
		}
	}
	
	private void criarConfiguracaoDoProduto(String s) {
		ConfigManager cm 	= pl.getConfGrupos();
		if(cm != null) {
			List<String> cmdsExecutar = new ArrayList<String>();
			cmdsExecutar.add("gerarvip "+s+" @dias @qnt @player");
			List<String> msgEnviar = new ArrayList<String>();
			msgEnviar.add("&eOla &a@player");
			msgEnviar.add("&eO produto que voce adquiriu (&a@produto&e) foi ativado!");
			msgEnviar.add("&eDias: &a@dias");
			msgEnviar.add("&eQuantidade: &a@qnt");
			cm.set("Grupos."+s+".Ativado", false);
			cm.set("Grupos."+s+".Ativar_Com_Player_Offline", false);
			cm.set("Grupos."+s+".Enviar_Mensagem", false);
			cm.set("Grupos."+s+".Mensagem_Receber_Ao_Ativar_Produto", msgEnviar);
			cm.set("Grupos."+s+".Money", false);
			cm.set("Grupos."+s+".Quantidade_De_Money", 0);
			cm.set("Grupos."+s+".Cmds_A_Executar", cmdsExecutar);
			pl.getProdutosConfigurados().add(s);
			Bukkit.getConsoleSender().sendMessage("§3[LojaSquare] §bNovo grupo de produto identificado e pre-configurado: §a"+s);
		}
	}
	
}
