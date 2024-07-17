package br.com.lojasquare.qrcode.listener;

import br.com.lojasquare.qrcode.LojaSquare;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class CommandListener implements Listener{
	
	private LojaSquare pl;
	
	@EventHandler
	public void preActive(PlayerCommandPreprocessEvent e){
		Player sender = e.getPlayer();
		String cmd = e.getMessage().split(" ")[0];
		String msg = e.getMessage();
		String[] args = Arrays.copyOfRange(msg.split(" "), 1, msg.split(" ").length);
		if(cmd.equalsIgnoreCase(pl.getMsg("Config.Cmd_Abrir_Menu_GUI"))) {
			e.setCancelled(true);
			if(!sender.hasPermission("lojasquare.qrcode.usar")) {
				sender.sendMessage(pl.getMsg("Msg.Sem_Permissao"));
				return;
			}
			ItemStack isInHand = sender.getItemInHand();
			if(Objects.nonNull(isInHand) && !Material.AIR.equals(isInHand.getType())) {
				sender.sendMessage(pl.getMsg("Msg.Remova_O_Item_Da_Mao"));
				return ;
			}
			pl.getOpenGuiPrincipal().execute(sender);
		}
	}
	
}
