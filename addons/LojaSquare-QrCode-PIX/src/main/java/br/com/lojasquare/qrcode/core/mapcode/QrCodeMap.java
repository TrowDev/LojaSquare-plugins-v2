package br.com.lojasquare.qrcode.core.mapcode;

import br.com.lojasquare.qrcode.utils.bukkit.Item;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class QrCodeMap {

    public static BufferedImage generateQRCode(String data) throws UnsupportedEncodingException, WriterException {
        BitMatrix matrix = new MultiFormatWriter()
                .encode(new String(data.getBytes("UTF-8"), "UTF-8"), BarcodeFormat.QR_CODE, 128, 128);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    public static void generateMap(final BufferedImage image, Player player, String nomeMapa, List<String> lore) {
        ItemStack mapItem = createMapItem();
        MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
        MapView mapView = Bukkit.createMap(player.getWorld());

        configureMapView(mapView);

        mapView.getRenderers().clear();
        mapView.addRenderer(new QRCodeMapRenderer(image));

        setMapMeta(mapMeta, mapView, mapItem);
        mapMeta.setDisplayName(nomeMapa);
        mapMeta.setLore(lore);
        mapItem.setItemMeta(mapMeta);

        player.getInventory().setItemInHand(mapItem);
    }

    private static ItemStack createMapItem() {
        String mapMaterial = Item.isAboveBukkit111() ? "FILLED_MAP" : "MAP";
        return new ItemStack(Material.getMaterial(mapMaterial));
    }

    private static void configureMapView(MapView mapView) {
        mapView.setScale(MapView.Scale.CLOSEST);
        if (Item.isAboveBukkit111()) {
            invokeMethodIfExists(mapView, "setUnlimitedTracking", true);
        }
    }

    private static void setMapMeta(MapMeta mapMeta, MapView mapView, ItemStack mapItem) {
        if (Item.isAboveBukkit111()) {
            invokeMethodIfExists(mapMeta, "setMapView", mapView);
        } else {
            // For older versions, set the map ID by modifying the ItemStack directly
            short mapID = getMapID(mapView);
            setMapItemID(mapItem, mapID);
        }
    }

    private static void setMapItemID(ItemStack mapItem, short mapID) {
        try {
            Method setDurabilityMethod = ItemStack.class.getMethod("setDurability", short.class);
            setDurabilityMethod.invoke(mapItem, mapID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void invokeMethodIfExists(Object obj, String methodName, Object... args) {
        try {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }
            Method method = getMethod(obj, methodName, parameterTypes);
            if(Objects.isNull(method)) return;
            method.invoke(obj, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Method getMethod(Object obj, String method, Class<?>... parameterTypes) {
        try {
            return obj.getClass().getMethod(method, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }

    private static short getMapID(MapView view) {
        try {
            return (short) view.getId();
        } catch (NoSuchMethodError e) {
            return getMapIDUsingReflection(view);
        }
    }

    private static short getMapIDUsingReflection(MapView view) {
        try {
            Method getIdMethod = view.getClass().getMethod("getId");
            Object mapID = getIdMethod.invoke(view);
            return (short) mapID;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    private static class QRCodeMapRenderer extends MapRenderer {
        private final BufferedImage image;

        public QRCodeMapRenderer(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
            mapCanvas.drawImage(0, 0, image);
        }
    }
}
