package br.com.lojasquare.core.autoconfig;

import br.com.lojasquare.LojaSquare;
import br.com.lojasquare.core.CheckService;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.ConfigManager;
import br.com.lojasquare.utils.StringUtils;
import br.com.lojasquare.utils.model.ProdutoInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.ConsoleCommandSender;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CheckCreateGroupItem implements CheckService {
	
	@Getter private LojaSquare pl;
	@Getter private ILSProvider lsProvider;
	
	public void execute(ConsoleCommandSender b) {
		b.sendMessage("§3[LojaSquare] §bIniciando checagem automatica de grupos e produtos...");
		List<String> gruposConfigPlugin 			= pl.getProdutosConfigurados();
		if(gruposConfigPlugin != null) {
			for(ProdutoInfo produto : getListaGruposProdutosDaLoja()) {
				if(gruposConfigPlugin.contains(produto.getGrupo())) continue;
				criarConfiguracaoDoProduto(produto,b);
			}
		}
	}

	public List<ProdutoInfo> getListaGruposProdutosDaLoja() {
		return lsProvider.getTodosProdutosDaLoja(pl.getLojaSquare().getTokenServidor());
	}
	
	private void criarConfiguracaoDoProduto(ProdutoInfo produto,ConsoleCommandSender b) {
		ConfigManager cm 	= pl.getConfGrupos();
		if(cm != null) {
			String valorFormatado = StringUtils.formatar(produto.getValor());
			String s = produto.getGrupo();
			List<String> cmdsExecutar = new ArrayList<String>();
			cmdsExecutar.add("gerarvip "+s+" @dias @qnt @player");
			List<String> lore = new ArrayList<String>();
			lore.add("&eGrupo: &a@produto&e");
			lore.add("&eDias: &a30 dias");
			lore.add("&eValor: &aR$ "+ valorFormatado);
			cm.set("Grupos."+s+".Ativado", false);
			cm.set("Grupos."+s+".Valor", valorFormatado);
			cm.set("Grupos."+s+".ID", "41:0");
			cm.set("Grupos."+s+".Nome", produto.getProduto());
			cm.set("Grupos."+s+".Lore", lore);
			pl.getProdutosConfigurados().add(s);
			b.sendMessage("§3[LojaSquare] §bNovo grupo de produto identificado e pre-configurado: §a"+s);
		}
	}
	
}
