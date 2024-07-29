package br.com.lojasquare.qrcode.utils.model;

import br.com.lojasquare.qrcode.utils.bukkit.Item;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Builder
@Data
@ToString
public class ProdutoInfoGUI {
    private String grupo;
    private String produto;
    private String itemId;
    private String itemNome;
    private List<String> lore;
    private ItemStack item;
    private Long produtoId;
    private String valor;
    private double valorSemFormatacao;
    private int slot;
    private int linha;
    private int quantidade;

    public ItemStack getItem() {
        return Item.getItemStack(itemId, itemNome, lore);
    }

    public ProdutoInfoGUI clonar() {
        return ProdutoInfoGUI.builder()
                .produto(getProduto()).itemId(getItemId()).itemNome(getItemNome())
                .lore(getLore()).item(getItem()).produtoId(getProdutoId())
                .valor(getValor()).valorSemFormatacao(getValorSemFormatacao())
                .slot(getSlot()).linha(getLinha()).quantidade(getQuantidade())
                .build();
    }
}
