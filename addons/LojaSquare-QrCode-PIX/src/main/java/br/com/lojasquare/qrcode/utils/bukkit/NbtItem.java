package br.com.lojasquare.qrcode.utils.bukkit;

import br.com.lojasquare.qrcode.utils.Constants;
import de.tr7zw.nbtapi.NBTItem;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class NbtItem {
    public NBTItem getNbtItem(ItemStack is, boolean setarFlagItemQrCode) {
        try {
            NBTItem nb = new NBTItem(is);
            if(setarFlagItemQrCode) {
                nb.setBoolean(Constants.KEY_ITEM_NBTAPI_QRCODE, true);
            }
            return nb;
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("§4[LsQrCode] §cFalha ao gerar NBTItem do item: §a"+is);
            return null;
        }
    }
}
