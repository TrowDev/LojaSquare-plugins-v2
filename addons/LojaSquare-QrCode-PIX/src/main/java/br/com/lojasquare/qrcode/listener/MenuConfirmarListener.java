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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class MenuConfirmarListener implements Listener {

    private LojaSquare pl;

    @EventHandler
    public void guiConfirmacao(InventoryClickEvent e) {
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
                    if(pl.getItemValidation().validaItemGuiConfirmacao(is, "Confirmar_GerarQrcode")) {
                        AcaoMenuConfirmarEnum.GERAR_QRCODE.getAcao().executar(p, inv);
                    } else if(pl.getItemValidation().validaItemGuiConfirmacao(is, "Voltar_Menu_Principal")) {
                        AcaoMenuConfirmarEnum.VOLTAR_MENU.getAcao().executar(p, inv);
                    } else if(pl.getItemValidation().validaItemGuiConfirmacao(is, "Aumentar_Quantidade")) {
                        AcaoMenuConfirmarEnum.AUMENTAR_QUANTIDADE.getAcao().executar(p, inv);
                    } else if(pl.getItemValidation().validaItemGuiConfirmacao(is, "Diminuir_Quantidade")) {
                        AcaoMenuConfirmarEnum.DIMINUIR_QUANTIDADE.getAcao().executar(p, inv);
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

}
