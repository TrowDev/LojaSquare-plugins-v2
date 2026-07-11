package br.com.lojasquare.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import br.com.lojasquare.utils.model.ItemInfo;

public interface ProductPreActiveCallback {
    Event<ProductPreActiveCallback> EVENT = EventFactory.createArrayBacked(
            ProductPreActiveCallback.class,
            (listeners) -> (player, item) -> {
                for (ProductPreActiveCallback listener : listeners) {
                    ActionResult result = listener.onPreActive(player, item);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            }
    );

    ActionResult onPreActive(ServerPlayerEntity player, ItemInfo item);
}
