package br.com.lojasquare.qrcode.core.gui.acoes;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface AcaoStrategy {
    void executar(Player p, Inventory inv);
}
