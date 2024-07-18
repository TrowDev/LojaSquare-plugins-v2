package br.com.lojasquare.qrcode.utils.versions.impl;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.utils.Constants;
import br.com.lojasquare.qrcode.utils.bukkit.NbtItem;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfoGUI;
import br.com.lojasquare.qrcode.utils.versions.ItemValidation;
import de.tr7zw.nbtapi.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class ItemValidationBukkit_v_1_8 implements ItemValidation {

    private LojaSquare pl;

    @Override
    public boolean validaItemGuiConfirmacao(ItemStack is, String nomeItemConfig) {
        if(!isItemGui(null, is)) return false;
        NBTItem nb = NbtItem.getNbtItem(is, false);
        if(nomeItemConfig.equals("Voltar_Menu_Principal") && nb.hasKey(Constants.KEY_ITEM_NBTAPI_QRCODE_VOLTAR_MENU_ANTERIOR)) {
            return nb.getBoolean(Constants.KEY_ITEM_NBTAPI_QRCODE_VOLTAR_MENU_ANTERIOR);
        } else if(nomeItemConfig.equals("Confirmar_GerarQrcode") && nb.hasKey(Constants.KEY_ITEM_NBTAPI_QRCODE_CONFIRMA_QRCODE)) {
            return nb.getBoolean(Constants.KEY_ITEM_NBTAPI_QRCODE_CONFIRMA_QRCODE);
        } else if(nb.hasKey(Constants.KEY_ITEM_NBTAPI_QRCODE_ALTERA_QTD)) {
            if(nomeItemConfig.equals("Aumentar_Quantidade") && nb.getBoolean(Constants.KEY_ITEM_NBTAPI_QRCODE_ADD_QTD)) return true;
            return nomeItemConfig.equals("Diminuir_Quantidade") && !nb.getBoolean(Constants.KEY_ITEM_NBTAPI_QRCODE_ADD_QTD);
        }
        return false;
    }

    @Override
    public boolean isItemGui(Inventory inv, ItemStack is) {
        if(Objects.nonNull(is)) {
            NBTItem nb = NbtItem.getNbtItem(is, false);
            return nb.hasKey(Constants.KEY_ITEM_NBTAPI_QRCODE);
        }
        return false;
    }

    @Override
    public boolean isItemCompletGUI(ItemStack is) {
        if(!isItemGui(null, is)) return false;
        NBTItem nb = NbtItem.getNbtItem(is, false);
        return nb.hasKey(Constants.KEY_ITEM_NBTAPI_QRCODE_AUTO_COMPLETE_GUI);
    }

    @Override
    public ProdutoInfoGUI getProdutoInfo(Inventory inv, ItemStack is) {
        if(!isItemGui(null, is)) return null;
        NBTItem nb = NbtItem.getNbtItem(is, false);
        if(!nb.hasKey(Constants.KEY_ITEM_NBTAPI_QRCODE_PRODUTOID)) return null;
        Long prdID = nb.getLong(Constants.KEY_ITEM_NBTAPI_QRCODE_PRODUTOID);

        return pl.getListaProdutos().stream().filter(produtoInfoGUI -> produtoInfoGUI.getProdutoId().equals(prdID))
                .findFirst().orElseGet(null);
    }
}
