package br.com.lojasquare.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import br.com.lojasquare.utils.model.ItemInfo;

public interface ProductActiveCallback {
    Event<ProductActiveCallback> EVENT = EventFactory.createArrayBacked(
            ProductActiveCallback.class,
            (listeners) -> (player, item) -> {
                for (ProductActiveCallback listener : listeners) {
                    ActionResult result = listener.onActive(player, item);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            }
    );

    ActionResult onActive(ServerPlayerEntity player, ItemInfo item);
}
