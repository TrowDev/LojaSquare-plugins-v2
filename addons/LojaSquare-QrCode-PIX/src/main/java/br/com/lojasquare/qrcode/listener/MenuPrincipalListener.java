package br.com.lojasquare.qrcode.listener;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.core.gui.OpenGuiConfirmar;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfoGUI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class MenuPrincipalListener implements Listener {
    private LojaSquare pl;

    @EventHandler
    public void guiPrincipal(InventoryClickEvent e) {
        if(e.getWhoClicked() instanceof Player) {
            Player p = (Player)e.getWhoClicked();
            Inventory inv = e.getInventory();
            if(Objects.nonNull(e.getCurrentItem()) && !Material.AIR.equals(e.getCurrentItem().getType())) {
                ItemStack is = e.getCurrentItem();
                if(pl.getItemValidation().isItemCompletGUI(is)) {
                    e.setCancelled(true);
                    return;
                }
                boolean isItemGui = pl.getItemValidation().isItemGui(inv, is);
                if(isItemGui) {
                    e.setCancelled(true);
                    ProdutoInfoGUI produtoInfoGUI = pl.getItemValidation().getProdutoInfo(inv, is);
                    if(Objects.nonNull(produtoInfoGUI)) {
                        pl.getOpenGuiConfirmar().execute(p, produtoInfoGUI.clonar());
                    }
                }
            }
        }
    }

}
