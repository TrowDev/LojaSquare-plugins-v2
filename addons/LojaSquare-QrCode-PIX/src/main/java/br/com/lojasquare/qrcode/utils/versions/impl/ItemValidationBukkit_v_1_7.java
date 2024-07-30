package br.com.lojasquare.qrcode.utils.versions.impl;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.utils.StringUtils;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfoGUI;
import br.com.lojasquare.qrcode.utils.versions.ItemValidation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class ItemValidationBukkit_v_1_7 implements ItemValidation {

    private LojaSquare pl;

    @Override
    public boolean isItemGui(Inventory inv, ItemStack is) {
        String nomeItem = StringUtils.removerCaracterCor(is.getItemMeta().getDisplayName());
        if (StringUtils.isEmpty(nomeItem)) return false;
        if (nomeItem.equals("§8AC")) return true;
        String nomeGui = StringUtils.removerCaracterCor(getInventoryTitle(inv));
        if (StringUtils.isEmpty(nomeGui)) return false;
        if (isItemGuiPrincipal(is, nomeGui, nomeItem)) return true;
        return isItemGuiConfirmacao(is, nomeGui, nomeItem);
    }

    private boolean isItemGuiConfirmacao(ItemStack is, String nomeGui, String nomeItem) {
        if (!nomeGui.equalsIgnoreCase(StringUtils.removerCaracterCor(pl.getMsg("GUI.Confirmar.Nome")))) return false;
        return Objects.nonNull(getProdutoInfoGUI(is, nomeItem)) ||
                validaItemGuiConfirmacao(is, "Confirmar_GerarQrcode") ||
                validaItemGuiConfirmacao(is, "Voltar_Menu_Principal") ||
                validaItemGuiConfirmacao(is, "Aumentar_Quantidade") ||
                validaItemGuiConfirmacao(is, "Diminuir_Quantidade");
    }

    @Override
    public boolean validaItemGuiConfirmacao(ItemStack is, String nomeItemConfig) {
        String prefix = "GUI.Confirmar.Item_" + nomeItemConfig;
        String nomeItem = StringUtils.removerCaracterCor(is.getItemMeta().getDisplayName());
        String idItem = is.getType().name() + ":" + is.getDurability();
        if (!idItem.equals(pl.getMsg(prefix + ".ID"))) return false;
        return nomeItem.equalsIgnoreCase(StringUtils.removerCaracterCor(pl.getMsg(prefix + ".Nome")));
    }

    private boolean isItemGuiPrincipal(ItemStack is, String nomeGui, String nomeItem) {
        if (!nomeGui.equalsIgnoreCase(StringUtils.removerCaracterCor(pl.getMsg("GUI.Principal.Nome")))) return false;
        return Objects.nonNull(getProdutoInfoGUI(is, nomeItem));
    }

    @Override
    public boolean isItemCompletGUI(ItemStack is) {
        if (Objects.isNull(is.getItemMeta()) || StringUtils.isEmpty(is.getItemMeta().getDisplayName())) return false;
        String nomeItem = is.getItemMeta().getDisplayName();
        return nomeItem.equals("§8AC");
    }

    @Override
    public ProdutoInfoGUI getProdutoInfo(Inventory inv, ItemStack is) {
        String nomeItem = StringUtils.removerCaracterCor(is.getItemMeta().getDisplayName());
        if (StringUtils.isEmpty(nomeItem)) return null;
        if (nomeItem.equals("§8AC")) return null;
        String nomeGui = StringUtils.removerCaracterCor(getInventoryTitle(inv));
        if (StringUtils.isEmpty(nomeGui)) return null;
        if (!nomeGui.equalsIgnoreCase(StringUtils.removerCaracterCor(pl.getMsg("GUI.Principal.Nome")))) return null;
        return getProdutoInfoGUI(is, nomeItem);
    }

    private ProdutoInfoGUI getProdutoInfoGUI(ItemStack is, String nomeItem) {
        String idDataItem = is.getType().name() + ":" + is.getDurability();
        for (ProdutoInfoGUI produto : pl.getListaProdutos()) {
            String nomeProduto = StringUtils.removerCaracterCor(produto.getItemNome());
            if (nomeItem.equalsIgnoreCase(nomeProduto) && idDataItem.equals(produto.getItemId())) {
                return produto;
            }
        }
        return null;
    }

    private String getInventoryTitle(Inventory inv) {
        try {
            Method getTitleMethod = inv.getClass().getMethod("getTitle");
            return (String) getTitleMethod.invoke(inv);
        } catch (Exception e) {
            try {
                Method getNameMethod = inv.getClass().getMethod("getName");
                return (String) getNameMethod.invoke(inv);
            } catch (Exception ex) {
                ex.printStackTrace();
                return "";
            }
        }
    }
}
