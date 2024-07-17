package br.com.lojasquare.qrcode.utils.bukkit.idlibrary;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

@AllArgsConstructor
public class IDMain {

    private final IDList idList;
    public static final String SEPARATOR = ":";
    private static final int SPAWN_EGG_ID = 383;

    private boolean isInt(String ID) {
        try {
            Integer.parseInt(ID);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isLegacy() {
        try {
            Material.valueOf("CONDUIT");
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    public Material getMaterial(String ID) {
        if (isLegacy()) {
            ItemStack itemStack = getItemStack(ID);
            return itemStack == null ? null : itemStack.getType();
        }

        ID = ID.replace(" ", "").toUpperCase();
        Material material = idList.getMaterial(ID + ":0");
        if (material != null) {
            return material;
        }

        try {
            material = Material.valueOf(ID);
            if (idList.getIDData(material) != null) {
                return material;
            }
        } catch (IllegalArgumentException ignored) {}

        if (ID.startsWith("383")) {
            return null;
        }

        if (!ID.contains(SEPARATOR) && !isInt(ID)) {
            try {
                material = Material.valueOf(ID);
                if (material != null) {
                    return material;
                }
            } catch (IllegalArgumentException ignored) {}
        }

        if (ID.contains(SEPARATOR)) {
            String[] ids = ID.split(SEPARATOR);
            try {
                return getMaterial(Integer.parseInt(ids[0]), Byte.parseByte(ids[1]));
            } catch (NumberFormatException e) {
                Material legacyMaterial = getLegacyMaterial(ids[0]);
                if (legacyMaterial != null) {
                    try {
                        return getMaterial(legacyMaterial.getId(), Byte.parseByte(ids[1]));
                    } catch (NumberFormatException ignored) {}
                }
                return null;
            }
        }

        try {
            return getMaterial(Integer.parseInt(ID));
        } catch (NumberFormatException e) {
            Material legacyMaterial = getLegacyMaterial(ID);
            return legacyMaterial == null ? null : getMaterial(legacyMaterial.getId(), (byte) 0);
        }
    }

    private Material getMaterial(int ID) {
        return getMaterial(ID, (byte) 0);
    }

    private Material getMaterial(int ID, byte data) {
        for (Material material : EnumSet.allOf(Material.class)) {
            if (material.getId() == ID) {
                Material resultMaterial = getMaterialFromLegacy(material, data);
                return resultMaterial == Material.AIR && (ID != 0 || data != 0) || data != 0 && resultMaterial.equals(getMaterialFromLegacy(material, (byte) 0))
                        ? idList.getMaterial(ID + SEPARATOR + data)
                        : resultMaterial;
            }
        }
        return null;
    }

    public ItemStack getItemStack(String ID) {
        if (isLegacy()) {
            ID = ID.replace(" ", "").toUpperCase();
            if (!ID.contains(SEPARATOR)) {
                ID += ":0";
            }
            String[] ids = ID.split(SEPARATOR);
            int id;

            try {
                id = Integer.parseInt(ids[0]);
            } catch (NumberFormatException e) {
                try {
                    id = Material.valueOf(ids[0]).getId();
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }

            for (Material material : EnumSet.allOf(Material.class)) {
                if (material.getId() == id) {
                    return new ItemStack(material, 1, (short) Integer.parseInt(ids[1]));
                }
            }
            return null;
        }

        Material material = getMaterial(ID);
        return material == null ? null : new ItemStack(material, 1);
    }

    public String getIDData(Material material) {
        byte data = getData(material);
        return getID(material) + (data == 0 ? "" : SEPARATOR + data);
    }

    public int getID(Material material) {
        if (isLegacy()) {
            return material.getId();
        }

        int id = idList.getID(material);
        if (id != -1) {
            return id;
        }

        Material legacyMaterial = getLegacyMaterial(material);
        int legacyId = legacyMaterial.getId();
        return (legacyId != SPAWN_EGG_ID) && (legacyId != 0 || material == Material.AIR) ? legacyId : 0;
    }

    public byte getData(Material material) {
        if (isLegacy()) {
            return 0;
        }

        byte data = idList.getData(material);
        if (data != -1) {
            return data;
        }

        Material legacyMaterial = getLegacyMaterial(material);
        int legacyId = legacyMaterial.getId();
        return (legacyId != SPAWN_EGG_ID) && (legacyId != 0 || material == Material.AIR) ? getData(material, legacyId) : 0;
    }

    private byte getData(Material material, int id) {
        for (Material mat : EnumSet.allOf(Material.class)) {
            if (mat.getId() == id) {
                for (byte data = 0; data <= 15; data++) {
                    if (material.equals(getMaterialFromLegacy(mat, data))) {
                        return data;
                    }
                }
            }
        }
        return 0;
    }

    private Material getMaterialFromLegacy(Material mat, byte data) {
        try {
            Method fromLegacyMethod = Bukkit.getUnsafe().getClass().getMethod("fromLegacy", MaterialData.class);
            if (fromLegacyMethod != null) {
                Object result = fromLegacyMethod.invoke(Bukkit.getUnsafe(), new MaterialData(mat, data));
                if (result instanceof Material) {
                    return (Material) result;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Handle exceptions or fallback to alternative logic
            e.printStackTrace();
        }
        return mat;
    }

    private Material getLegacyMaterial(Material material) {
        try {
            // Tenta obter o método toLegacy da classe UnsafeValues do Bukkit
            Method toLegacyMethod = Bukkit.getUnsafe().getClass().getMethod("toLegacy", Material.class);
            if (toLegacyMethod != null) {
                Object result = toLegacyMethod.invoke(Bukkit.getUnsafe(), material);
                if (result instanceof Material) {
                    return (Material) result;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Trate exceções ou implemente uma lógica alternativa, se necessário
            e.printStackTrace();
        }

        // Fallback: Se não conseguir usar toLegacy, retorna o próprio material
        return material;
    }

    private Material getLegacyMaterial(String id) {
        Material legacyMaterial = null;

        try {
            // Tenta chamar o método getMaterial(String) para versões mais antigas
            Method getMaterialMethod = Material.class.getMethod("getMaterial", String.class);
            legacyMaterial = (Material) getMaterialMethod.invoke(null, "LEGACY_" + id);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Trate exceções de reflexão
            e.printStackTrace();
        }

        if (legacyMaterial == null) {
            try {
                // Tenta chamar o método getMaterial(String, boolean) para versões mais recentes
                Method getMaterialMethod = Material.class.getMethod("getMaterial", String.class, boolean.class);
                legacyMaterial = (Material) getMaterialMethod.invoke(null, "LEGACY_" + id, false);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Trate exceções de reflexão
                e.printStackTrace();
            }
        }

        return legacyMaterial;
    }

    public String getIDData(ItemStack itemStack) {
        byte data = getData(itemStack);
        return getID(itemStack) + (data == 0 ? "" : SEPARATOR + data);
    }

    public int getID(ItemStack itemStack) {
        return isLegacy() ? itemStack.getType().getId() : getID(itemStack.getType());
    }

    public byte getData(ItemStack itemStack) {
        return isLegacy() ? itemStack.getData().getData() : getData(itemStack.getType());
    }
}
