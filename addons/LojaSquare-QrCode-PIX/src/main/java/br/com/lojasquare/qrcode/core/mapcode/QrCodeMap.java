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
        try {
            if (image == null) {
                Bukkit.getLogger().severe("The QR code image is null.");
                return;
            }

            ItemStack mapItem = createMapItem();
            MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
            MapView mapView = Bukkit.createMap(player.getWorld());
            mapView.setScale(MapView.Scale.CLOSEST);

            if (Item.isAboveBukkit111()) {
                mapView.setUnlimitedTracking(true);
            }

            mapView.getRenderers().clear();
            mapView.addRenderer(new QRCodeMapRenderer(image));

            if (Item.isAboveBukkit111()) {
                mapMeta.setMapView(mapView);
            } else {
                short mapID = getMapID(mapView);
                setMapItemID(mapItem, mapID);
            }

            mapMeta.setDisplayName(nomeMapa);
            mapMeta.setLore(lore);
            mapItem.setItemMeta(mapMeta);

            player.getInventory().setItemInHand(mapItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ItemStack createMapItem() {
        Material mapMaterial = Item.isAboveBukkit111() ? Material.FILLED_MAP : Material.MAP;
        return new ItemStack(mapMaterial);
    }

    private static void setMapItemID(ItemStack mapItem, short mapID) {
        try {
            Method setDurabilityMethod = ItemStack.class.getMethod("setDurability", short.class);
            setDurabilityMethod.invoke(mapItem, mapID);
        } catch (Exception e) {
            e.printStackTrace();
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
        private boolean rendered = false;

        public QRCodeMapRenderer(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
            if (!rendered) {
                mapCanvas.drawImage(0, 0, image);
                rendered = true;
            }
        }
    }
}
