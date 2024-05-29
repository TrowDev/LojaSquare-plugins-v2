package br.com.lojasquare.core.autoconfig;

import br.com.lojasquare.LojaSquare;
import br.com.lojasquare.core.CheckService;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.ConfigManager;
import br.com.lojasquare.utils.Constants;
import br.com.lojasquare.utils.model.ProdutoInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.ConsoleCommandSender;

import java.util.ArrayList;
import java.util.List;

import static br.com.lojasquare.utils.Constants.PREFIXO;

@AllArgsConstructor
public class CheckCreateGroupItem implements CheckService {
	
	@Getter private LojaSquare pl;
	@Getter private ILSProvider lsProvider;
	
	public void execute(ConsoleCommandSender b) {
		b.sendMessage(PREFIXO+" §bIniciando checagem automatica de grupos e produtos...");
		List<String> gruposConfigPlugin 			= pl.getProdutosConfigurados();
		if(gruposConfigPlugin != null) {
			List<String> gruposDeProdutosNoSite 	= getListaGruposProdutosDaLoja();
			if(gruposDeProdutosNoSite != null) {
				for(String grupoSite : gruposDeProdutosNoSite) {
					if(gruposConfigPlugin.contains(grupoSite)) continue;
					criarConfiguracaoDoProduto(grupoSite,b);
				}
			}
		}
	}

	public List<String> getListaGruposProdutosDaLoja() {
		List<ProdutoInfo> prods = lsProvider.getTodosProdutosDaLoja();
		List<String> grupos 	= new ArrayList<String>();
		if(prods != null) {
			for(ProdutoInfo pi : prods) {
				if(grupos.contains(pi.getGrupo())) continue;
				grupos.add(pi.getGrupo());
			}
		}
		return grupos;
	}
	
	private void criarConfiguracaoDoProduto(String s,ConsoleCommandSender b) {
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
			b.sendMessage(PREFIXO+" §bNovo grupo de produto identificado e pre-configurado: §a"+s);
		}
	}
	
}
