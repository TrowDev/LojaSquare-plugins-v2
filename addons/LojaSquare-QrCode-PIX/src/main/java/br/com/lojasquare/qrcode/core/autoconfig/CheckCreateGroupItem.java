package br.com.lojasquare.qrcode.core.autoconfig;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.core.CheckService;
import br.com.lojasquare.qrcode.providers.lojasquare.ILSProvider;
import br.com.lojasquare.qrcode.utils.bukkit.ConfigManager;
import br.com.lojasquare.qrcode.utils.StringUtils;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.ConsoleCommandSender;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class CheckCreateGroupItem implements CheckService {
	
	private LojaSquare pl;
	private ILSProvider lsProvider;
	
	public void execute(ConsoleCommandSender b) {
		b.sendMessage("§3[LSQrCode] §bIniciando checagem automatica de grupos e produtos...");
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
			lore.add("&eProduto: &a"+produto.getProduto()+"&e");
			lore.add("&eGrupo: &a"+produto.getGrupo()+"&e");
			lore.add("&eDias: &a30 dias");
			lore.add("&eValor: &aR$ @valor");
			lore.add("&eQuantidade: &a@qtd");
			cm.set("Grupos."+s+".Ativado", false);
			cm.set("Grupos."+s+".Nome", produto.getProduto());
			cm.set("Grupos."+s+".ProdutoID", produto.getProdutoID());
			cm.set("Grupos."+s+".Valor", valorFormatado);
			cm.set("Grupos."+s+".Valor_Sem_Formatacao", produto.getValor());
			cm.set("Grupos."+s+".Linha", 1);
			cm.set("Grupos."+s+".Slot", 1);
			cm.set("Grupos."+s+".Item.ID", "41:0");
			cm.set("Grupos."+s+".Item.Nome", produto.getProduto());
			cm.set("Grupos."+s+".Item.Lore", lore);
			pl.getProdutosConfigurados().add(s);
			b.sendMessage("§3[LSQrCode] §bNovo grupo de produto identificado e pre-configurado: §a"+s);
		}
	}
	
}
