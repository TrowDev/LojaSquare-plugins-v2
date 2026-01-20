package br.com.lojasquare.core.autoconfig;

import br.com.lojasquare.LojaSquarePlugin;
import br.com.lojasquare.core.CheckService;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.ConfigManager;
import br.com.lojasquare.utils.model.ProdutoInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CheckCreateGroupItem implements CheckService {

    @Getter
    private final LojaSquarePlugin plugin;
    
    @Getter
    private final ILSProvider lsProvider;

    @Override
    public void execute() {
        plugin.log("[LojaSquare] Iniciando checagem automatica de grupos e produtos...");
        
        List<String> gruposConfigPlugin = plugin.getProdutosConfigurados();
        
        if (gruposConfigPlugin != null) {
            List<String> gruposDeProdutosNoSite = getListaGruposProdutosDaLoja();
            
            if (gruposDeProdutosNoSite != null) {
                for (String grupoSite : gruposDeProdutosNoSite) {
                    if (gruposConfigPlugin.contains(grupoSite)) continue;
                    criarConfiguracaoDoProduto(grupoSite);
                }
            }
        }
    }

    public List<String> getListaGruposProdutosDaLoja() {
        List<ProdutoInfo> prods = lsProvider.getTodosProdutosDaLoja();
        List<String> grupos = new ArrayList<>();
        
        if (prods != null) {
            for (ProdutoInfo pi : prods) {
                if (grupos.contains(pi.getGrupo())) continue;
                grupos.add(pi.getGrupo());
            }
        }
        
        return grupos;
    }

    private void criarConfiguracaoDoProduto(String grupoNome) {
        ConfigManager cm = plugin.getConfGrupos();
        
        if (cm != null) {
            List<String> cmdsExecutar = new ArrayList<>();
            cmdsExecutar.add("gerarvip " + grupoNome + " @dias @qnt @player");
            
            List<String> msgEnviar = new ArrayList<>();
            msgEnviar.add("&eOla &a@player");
            msgEnviar.add("&eO produto que voce adquiriu (&a@produto&e) foi ativado!");
            msgEnviar.add("&eDias: &a@dias");
            msgEnviar.add("&eQuantidade: &a@qnt");
            
            String basePath = "Grupos." + grupoNome;
            cm.set(basePath + ".Ativado", false);
            cm.set(basePath + ".Ativar_Com_Player_Offline", false);
            cm.set(basePath + ".Enviar_Mensagem", false);
            cm.set(basePath + ".Mensagem_Receber_Ao_Ativar_Produto", msgEnviar);
            cm.set(basePath + ".Money", false);
            cm.set(basePath + ".Quantidade_De_Money", 0);
            cm.set(basePath + ".Cmds_A_Executar", cmdsExecutar);
            
            plugin.getProdutosConfigurados().add(grupoNome);
            plugin.log("[LojaSquare] Novo grupo de produto identificado e pre-configurado: " + grupoNome);
        }
    }
}
