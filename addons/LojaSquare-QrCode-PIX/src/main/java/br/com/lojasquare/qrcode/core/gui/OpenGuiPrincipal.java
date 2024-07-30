package br.com.lojasquare.qrcode.core.gui;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.utils.Constants;
import br.com.lojasquare.qrcode.utils.StringUtils;
import br.com.lojasquare.qrcode.utils.bukkit.GUIAPI;
import br.com.lojasquare.qrcode.utils.bukkit.Item;
import br.com.lojasquare.qrcode.utils.bukkit.NbtItem;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfoGUI;
import de.tr7zw.nbtapi.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class OpenGuiPrincipal {

    @Getter
    private LojaSquare pl;

    public void execute(Player p) {
        int tamanho = pl.getConfig().getInt("GUI.Principal.Tamanho",9);
        if(tamanho < 9) {
            p.sendMessage("§4[LsQrCode] §cAvise o administrador que o menu esta com uma configuracao errada no tamanho do GUI.");
            return;
        }
        GUIAPI gui = new GUIAPI(tamanho, pl.getMsg("GUI.Principal.Nome"));
        popularMenuGuiComProdutosDaLista(p, gui);
        completarMenuGUISlotsVazios(gui);

        p.openInventory(gui.getInventory());
    }

    private void popularMenuGuiComProdutosDaLista(Player p, GUIAPI gui) {
        for(ProdutoInfoGUI produto : pl.getListaProdutos()) {
            ItemStack item = getItemGUI(p, produto);

            gui.setItem(produto.getSlot(), produto.getLinha(), item);
        }
    }

    private void completarMenuGUISlotsVazios(GUIAPI gui) {
        if(pl.getConfig().getBoolean("GUI.Principal.Completar_GUI")) {
            ItemStack is = Item.getItemStack(pl.getMsg("GUI.Principal.Item_ID_Completar_GUI"));
            if(Objects.isNull(is) || Objects.isNull(is.getItemMeta())) return;
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§8AC");
            is.setItemMeta(im);

            if(pl.isBukkitVersionAcima18()) {
                NBTItem nb = NbtItem.getNbtItem(is, true);
                nb.setBoolean(Constants.KEY_ITEM_NBTAPI_QRCODE_AUTO_COMPLETE_GUI, true);
                is = nb.getItem();
            }
            gui.completSlotEmpty(is);
        }
    }

    private ItemStack getItemGUI(Player p, ProdutoInfoGUI produto) {
        ItemStack item = produto.getItem();
        ItemMeta itemMeta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        for(String s : itemMeta.getLore()) {
            lore.add(s.replace("@player", p.getName())
                    .replace("@produto", produto.getProduto())
                    .replace("@qtd", produto.getQuantidade()+"")
                    .replace("@valor", StringUtils.formatar(produto.getValorSemFormatacao() * produto.getQuantidade())));
        }
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);

        if(pl.isBukkitVersionAcima18()) {
            NBTItem nb = NbtItem.getNbtItem(item, true);
            nb.setLong(Constants.KEY_ITEM_NBTAPI_QRCODE_PRODUTOID, produto.getProdutoId());
            item = nb.getItem();
        }

        return item;
    }
}
