package br.com.lojasquare.core.delivery;

import br.com.lojasquare.LojaSquare;
import br.com.lojasquare.api.ProductPreActiveEvent;
import br.com.lojasquare.core.CheckService;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.Constants;
import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

import static br.com.lojasquare.utils.Constants.PREFIXO;

@AllArgsConstructor
public class CheckDelivery implements CheckService {
	
	@Getter private LojaSquare pl;
	@Getter private ILSProvider lsProvider;
	
	public void execute(ConsoleCommandSender b){
		b.sendMessage(PREFIXO+" §bIniciando checagem automatica de entregas...");
		b.sendMessage(PREFIXO+" §bTempo de checagem a cada §a"+pl.getTempoChecarItens()+"§b segundos.");
		new BukkitRunnable() {
			public void run() {
				List<ItemInfo> itens = lsProvider.getTodasEntregas(LSEntregaStatus.PENDENTE);
				pl.printDebug("");
				pl.printDebug(PREFIXO+" §bItens Size: §a"+itens.size());
				if(itens!=null&&itens.size()>0){
					for(final ItemInfo item:itens){
						
						if(!validaRegrasAntesEntregarItem(item)) continue;
						
						Player p = Bukkit.getPlayer(item.getPlayer());
						
						callEventoEntregarItem(item, p);
					}
				}
			}
		}.runTaskTimerAsynchronously(pl, 20*10, 20*pl.getTempoChecarItens());
	}

	private void callEventoEntregarItem(final ItemInfo item, final Player p) {
		new BukkitRunnable() {
			public void run() {
				pl.printDebug(PREFIXO+" §bPre Product Active Event");
				ProductPreActiveEvent pae = new ProductPreActiveEvent(Objects.isNull(p) ? null : p, item);
				Bukkit.getPluginManager().callEvent(pae);
			}
		}.runTask(pl);
	}
	
	private boolean validaRegrasAntesEntregarItem(ItemInfo item) {
		if(Objects.isNull(item)) return false;
		
		if(item.getStatusID()==2) return false;
		
		String servidor = pl.getServidor();
		if(!checaServidorCorretoEntregarItem(item, servidor)) return false;
		
		pl.printDebug(PREFIXO+" §bItem: §a"+item.toString()+" §b// subServer: §e"+item.getSubServidor()+"§b // Servidor: §d"+servidor);
		final Player p = Bukkit.getPlayer(item.getPlayer());
		
		if(!checaItemNaConfig(item, p)) return false;
		
		pl.printDebug(PREFIXO+" §bPlayer: §a"+item.getPlayer()+"§b // P NULL? §a"+(p==null));
		if(!checaEntregarComPlayerOffline(item, p)) return false;
		
		pl.printDebug(PREFIXO+" §bCheca player: §a"+item.getPlayer()+"§b com inv vazio:");
		if(!checaPlayerInvVazio(item, p)) return false;
		
		return true;
	}

	private boolean checaPlayerInvVazio(ItemInfo item, final Player p) {
		if(!Objects.isNull(p) && pl.getConfGrupos()
				.getBoolean("Grupo."+item.getGrupo()+".Entregar_Apenas_Com_Inventario_Vazio",false)) {
			if(!isInventoryEmpty(p)) {
				p.sendMessage(pl.getMsg("Msg.Limpe_Seu_Inventario".replace("@grupo", item.getGrupo())));
				return false;
			}
		}
		return true;
	}

	private boolean checaItemNaConfig(final ItemInfo item, final Player p) {
		if(!pl.produtoAtivado(item.getGrupo())) {
			pl.printDebug(PREFIXO+" §bProduto §a"+item.getGrupo()+"§b nao configurado!");
			if(p != null) {
				p.sendMessage(pl.getMsg("Msg.Produto_Nao_Configurado").replace("@grupo", item.getGrupo()));
			}
			return false;
		}
		return true;
	}

	private boolean checaEntregarComPlayerOffline(final ItemInfo item, final Player p) {
		if(p==null) {
			if(!pl.getConfGrupos().getBoolean("Grupos."+item.getGrupo()+".Ativar_Com_Player_Offline",false)) {
				boolean disputa = item.getProduto().equalsIgnoreCase("DISPUTA") && item.getGrupo().equalsIgnoreCase("DISPUTA");
				boolean resolvido = item.getProduto().equalsIgnoreCase("RESOLVIDO") && item.getGrupo().equalsIgnoreCase("RESOLVIDO");
				if(!disputa && !resolvido) return false;
			}
		}
		return true;
	}

	private boolean checaServidorCorretoEntregarItem(final ItemInfo item, String servidor) {
		if(!item.getSubServidor().equalsIgnoreCase(servidor)) {
			pl.printDebug(PREFIXO+" §bItem: §a"+item.getProduto()+"§b do grupo §a"+item.getGrupo()+"§b esta configurado para o servidor §a"+item.getSubServidor()+"§b, porem o servidor atual e o §a"+servidor);
			return false;
		}
		return true;
	}
	
	public static boolean isInventoryEmpty(Player p){
		for(ItemStack item : p.getInventory().getContents()){
			if(item != null && item.getType()!=Material.AIR)
				return false;
		}
		for(ItemStack item : p.getInventory().getArmorContents()){
			if(item != null && item.getType()!=Material.AIR)
				return false;
		}
		return true;
	}

}
