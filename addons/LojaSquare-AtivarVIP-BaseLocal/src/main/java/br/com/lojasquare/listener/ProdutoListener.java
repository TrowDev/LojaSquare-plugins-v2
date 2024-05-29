package br.com.lojasquare.listener;

import br.com.lojasquare.LojaSquare;
import br.com.lojasquare.api.ProductActiveEvent;
import br.com.lojasquare.api.ProductPreActiveEvent;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.Constants;
import br.com.lojasquare.utils.model.ItemInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

import static br.com.lojasquare.utils.Constants.PREFIXO;
import static br.com.lojasquare.utils.Constants.PREFIXO_ERRO;

@AllArgsConstructor
public class ProdutoListener implements Listener{
	
	@Getter private LojaSquare pl;
	@Getter private ILSProvider lsProvider;
	
	@EventHandler
	public void preActive(final ProductPreActiveEvent e){
		pl.printDebug(PREFIXO+" §bpreActive");
		if(e.isCancelled()) return;
		new BukkitRunnable() {
			public void run() {
				pl.printDebug(PREFIXO+" §bAntes update delivery.");
				final ItemInfo ii = e.getItemInfo();
				if(lsProvider.updateDelivery(ii)){
					pl.printDebug("§6[LojaSquare-BD-Local] §ePreparando entrega do produto: §7"+ii.toString());
					new BukkitRunnable() {
						public void run() {
							ProductActiveEvent pae = new ProductActiveEvent(e.getPlayer(), ii);
							Bukkit.getPluginManager().callEvent(pae);
						}
					}.runTask(pl);
				}else{
					pl.print(PREFIXO_ERRO+" §cNao foi possivel atualizar o status da compra: §a"+ii.toString()+"§c para: 'Entregue'. Portanto, a entrega nao foi feita!");
				}
			}
		}.runTaskAsynchronously(pl);
	}
	
	public String replaceString(ItemInfo ii, String cmds, int qntMoneyInteiro, double qntMoney) {
		cmds=cmds.replace("@moneyInteiro", (qntMoneyInteiro>0?qntMoneyInteiro:"")+"")
				.replace("@money", (qntMoney>0?""+qntMoney:"")).replace("@grupo", ii.getGrupo());
		cmds=cmds.replace("@dias", ii.getDias()+"").replace("@player", ii.getPlayer());
		cmds=cmds.replace("@qnt", ii.getQuantidade()+"").replace("@produto", ii.getProduto());
		if(Objects.nonNull(ii.getCupom())) {
			cmds = cmds.replace("@cupom", ii.getCupom());
		} else {
			cmds = cmds.replace("@cupom", "SEM CUPOM");
		}
		return cmds;
	}
	
	@EventHandler
	public void activeDelivery(ProductActiveEvent e){
		ItemInfo ii = e.getItemInfo();
		if(e.isCancelled()){
			pl.print(PREFIXO_ERRO+" §cAtivacao da compra: §a"+ii.toString()+"§c foi cancelada por meio do evento §aProductActiveEvent§c"
					+ ", mas ja foi marcado no site com status 'Entregue'.");
			return;
		}
		List<String> listCmds = pl.getConfGrupos().getStringList("Grupos."+ii.getGrupo()+".Cmds_A_Executar");
		boolean isMoney = pl.getConfGrupos().getBoolean("Grupos."+ii.getGrupo()+".Money");
		boolean smartDelivery = pl.doSmartDelivery();
		double qntMoney = 0;
		if(isMoney){
			qntMoney = pl.getConfGrupos().getDouble("Grupos."+ii.getGrupo()+".Quantidade_De_Money");
		}
		if(smartDelivery){
			qntMoney *= ii.getQuantidade();
		}
		int qntMoneyInteiro = (int)qntMoney;
		if(smartDelivery) {
			listCmds.forEach(cmds -> dispatchCommandDelivery(ii, qntMoneyInteiro, cmds));
		} else {
			for(int i = 1; i<=ii.getQuantidade(); i++) {
				listCmds.forEach(cmds -> dispatchCommandDelivery(ii, qntMoneyInteiro, cmds));
			}
		}
		sendMsgToPlayerOnActiveProducts(ii, e.getPlayer());
		pl.printDebug(PREFIXO+" §bEntrega do produto §a"+ii.toString()+"§b concluida com sucesso!");
		pl.printDebug("");
	}

	private void dispatchCommandDelivery(ItemInfo ii, int qntMoneyInteiro, String cmds) {
		try {
			cmds = replaceString(ii, cmds, qntMoneyInteiro, qntMoneyInteiro);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmds);
		} catch (Exception e2) {
			pl.print(PREFIXO_ERRO+" §cErro ao executar o cmd §a" + cmds + "§c da entrega com ID: §a" + ii.getEntregaID() + "§c e codigo de transacao: §a" + ii.getCodigo() + "§c. Erro: §a" + e2.getMessage());
			if (pl.canDebug()) {
				e2.printStackTrace();
			}
		}
	}

	private void sendMsgToPlayerOnActiveProducts(ItemInfo ii, Player p){
		if(Objects.isNull(p)) return;
		if(pl.getConfGrupos().getBoolean("Grupos."+ii.getGrupo()+".Enviar_Mensagem",false)){
			for(String s:pl.getConfGrupos().getStringList("Grupos."+ii.getGrupo()+".Mensagem_Receber_Ao_Ativar_Produto")){
				s=s.replace("&", "§");
				s=replaceString(ii, s, 0, 0);
				p.sendMessage(s);
			}
		}
	}
	
}
