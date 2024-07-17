package br.com.lojasquare.qrcode.utils.versions;

import br.com.lojasquare.qrcode.utils.model.ProdutoInfoGUI;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface ItemValidation {
    boolean validaItemGuiConfirmacao(ItemStack is, String nomeItemConfig);
    boolean isItemGui(Inventory inv, ItemStack is);
    boolean isItemCompletGUI(ItemStack is);
    ProdutoInfoGUI getProdutoInfo(Inventory inv, ItemStack is);
}
