package br.com.lojasquare.qrcode.core.gui.acoes.impl;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.core.gui.acoes.AcaoStrategy;
import br.com.lojasquare.qrcode.utils.Constants;
import br.com.lojasquare.qrcode.utils.StringUtils;
import br.com.lojasquare.qrcode.utils.bukkit.NbtItem;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfoGUI;
import de.tr7zw.nbtapi.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class RemoverQuantidadeStrategyImpl implements AcaoStrategy {

    private LojaSquare pl;

    @Override
    public void executar(Player p, Inventory inv) {
        ProdutoInfoGUI produto = validaPlayerEscolhendoProduto(p);
        if (Objects.isNull(produto)) return;
        if(produto.getQuantidade() <= 1) {
            produto.setQuantidade(1);
            return;
        }
        produto.setQuantidade(produto.getQuantidade()-1);

        ItemStack item = getItemGUI(p, produto);

        inv.setItem(produto.getLinha() * 9 + produto.getSlot() - 1 - 9, item);
    }

    private ProdutoInfoGUI validaPlayerEscolhendoProduto(Player p) {
        if(!pl.getOpenGuiConfirmar().getListaNickProduto().containsKey(p.getName())) {
            p.sendMessage(pl.getMsg("Msg.Nao_Foi_Possivel_Identificar_Produto_Selecionado"));
            return null;
        }
        ProdutoInfoGUI produto = pl.getOpenGuiConfirmar().getListaNickProduto().get(p.getName());
        if(Objects.isNull(produto)) {
            p.sendMessage(pl.getMsg("Msg.Nao_Foi_Possivel_Identificar_Produto_Selecionado"));
            return null;
        }
        return produto;
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
            NBTItem nb = NbtItem.getNbtItem(item);
            nb.setLong(Constants.KEY_ITEM_NBTAPI_QRCODE_PRODUTOID, produto.getProdutoId());
            item = nb.getItem();
        }

        return item;
    }
}
