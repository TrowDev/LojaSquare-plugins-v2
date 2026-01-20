package br.com.lojasquare.api;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import br.com.lojasquare.utils.model.ItemInfo;

/**
 * Evento disparado antes de ativar um produto.
 * Equivalente ao ProductPreActiveEvent do Spigot, adaptado para Hytale.
 * 
 * Este evento pode ser cancelado por outros plugins.
 */
@Getter
public class ProductPreActiveEvent {

    private final UUID playerUUID;
    private final String playerName;
    private final ItemInfo itemInfo;

    @Setter
    private boolean cancelled;

    public ProductPreActiveEvent(UUID playerUUID, String playerName, ItemInfo itemInfo) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.itemInfo = itemInfo;
        this.cancelled = false;
    }
}
