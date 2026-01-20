package br.com.lojasquare.api;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import br.com.lojasquare.utils.model.ItemInfo;

/**
 * Evento disparado para ativar um produto após confirmação na API.
 * Equivalente ao ProductActiveEvent do Spigot, adaptado para Hytale.
 * 
 * Este evento pode ser cancelado, mas a entrega já foi marcada como concluída
 * na API.
 */
@Getter
public class ProductActiveEvent {

    private final UUID playerUUID;
    private final String playerName;
    private final ItemInfo itemInfo;

    @Setter
    private boolean cancelled;

    public ProductActiveEvent(UUID playerUUID, String playerName, ItemInfo itemInfo) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.itemInfo = itemInfo;
        this.cancelled = false;
    }
}
