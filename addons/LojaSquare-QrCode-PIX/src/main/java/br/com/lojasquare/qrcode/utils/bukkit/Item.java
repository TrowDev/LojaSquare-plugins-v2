package br.com.lojasquare.qrcode.utils.bukkit;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.utils.bukkit.idlibrary.IDMain;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Item {

    @Getter
    public static String version = "0.0";

    /**
     * Checks if the Bukkit version is 1.8 or above.
     *
     * @return true if version is 1.8 or above, false otherwise
     */
    public static boolean isAboveBukkit18() {
        return versionCompare(version, "1.8") >= 0;
    }

    public static boolean isAboveBukkit111() {
        return versionCompare(version, "1.11") >= 0;
    }

    /**
     * Gets the IDMain instance from LojaSquare.
     *
     * @return IDMain instance
     */
    public static IDMain getId() {
        return LojaSquare.getInstance().getIdMain();
    }

    /**
     * Defines the Bukkit version and checks for plugin compatibility.
     *
     * @return true if the version is defined and compatible, false otherwise
     */
    public static boolean defineVersion() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        Server server = Bukkit.getServer();
        Matcher matcher = Pattern.compile("(^[^\\-]*)").matcher(server.getBukkitVersion());

        if (!matcher.find()) {
            displayError(console, "§cCould not find Bukkit version... Disabling plugin");
            Bukkit.getPluginManager().disablePlugin(LojaSquare.getInstance());
            return false;
        }

        version = matcher.group(1);
        console.sendMessage("§3[" + LojaSquare.getInstance().getDescription().getName() + "] §bWe found Bukkit version! Version: " + version);
        return true;
    }

    private static void displayError(ConsoleCommandSender console, String message) {
        console.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        console.sendMessage("§3["+LojaSquare.getInstance().getDescription().getName()+"] §cDesativado...");
        console.sendMessage("§3Criador: §3Trow");
        console.sendMessage("§4Erro: §a"+message);
        console.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        Bukkit.getPluginManager().disablePlugin(LojaSquare.getInstance());
    }

    public static ItemStack getItemStack(String mat, String nome, List<String> lore) {
        ItemStack is = getItemStack(mat);
        if(Objects.isNull(is) || Objects.isNull(is.getItemMeta())) return null;
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(nome.replace("&", "§"));
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }

    /**
     * Gets an ItemStack from a material string.
     *
     * @param mat the material string
     * @return the corresponding ItemStack, or null if invalid
     */
    public static ItemStack getItemStack(String mat) {
        return getItemStack(mat, 1, (byte) 0);
    }

    /**
     * Gets an ItemStack from a material string with specified amount.
     *
     * @param mat the material string
     * @param amt the amount of items
     * @return the corresponding ItemStack, or null if invalid
     */
    public static ItemStack getItemStack(String mat, int amt) {
        return getItemStack(mat, amt, (byte) 0);
    }

    /**
     * Gets an ItemStack from a material string with specified amount and data.
     *
     * @param mat the material string
     * @param amt the amount of items
     * @param data the data value of the item
     * @return the corresponding ItemStack, or null if invalid
     */
    public static ItemStack getItemStack(String mat, int amt, byte data) {
        try {
            ItemStack is = null;
            if (versionCompare(version, "1.12") < 0) {
                return createLegacyItemStack(mat, amt, data);
            } else {
                Material material = getId().getMaterial(mat);
                return new ItemStack(material, amt, data);
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§4[" + LojaSquare.getInstance().getDescription().getName() + "] §cMaterial §a" + mat + "§c is not configured correctly.");
            return null;
        }
    }

    private static ItemStack createLegacyItemStack(String mat, int amt, byte data) {
        if (mat.contains(":")) {
            String[] parts = mat.split(":");
            return new ItemStack(Material.getMaterial(Integer.parseInt(parts[0])), amt, Byte.parseByte(parts[1]));
        } else {
            return new ItemStack(Material.getMaterial(Integer.parseInt(mat)), amt, data);
        }
    }

    /**
     * Compares two version strings.
     *
     * @param str1 the first version string
     * @param str2 the second version string
     * @return -1 if str1 is less than str2, 0 if equal, 1 if greater
     */
    public static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int length = Math.min(vals1.length, vals2.length);

        for (int i = 0; i < length; i++) {
            int diff = Integer.compare(Integer.parseInt(vals1[i]), Integer.parseInt(vals2[i]));
            if (diff != 0) {
                return diff;
            }
        }

        return Integer.compare(vals1.length, vals2.length);
    }
}
