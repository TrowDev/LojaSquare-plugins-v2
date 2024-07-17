package br.com.lojasquare.qrcode.listener;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.utils.enums.AcaoMenuConfirmarEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class FecharInventarioListener implements Listener {

    private LojaSquare pl;

    @EventHandler
    public void removerItensIndevidosDoInventario(InventoryCloseEvent e) {
        if(e.getPlayer() instanceof Player) {
            Player p = (Player)e.getPlayer();
            Inventory inv = p.getInventory();
            for(ItemStack is : inv.getContents()) {
                if(Objects.isNull(is) || Material.AIR.equals(is.getType())) continue;
                if(pl.getItemValidation().isItemGui(inv, is)) {
                    inv.removeItem(is);
                }
            }
        }
    }

}
