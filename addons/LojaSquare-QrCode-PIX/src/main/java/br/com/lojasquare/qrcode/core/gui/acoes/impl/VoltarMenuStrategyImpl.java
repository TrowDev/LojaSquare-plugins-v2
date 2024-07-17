package br.com.lojasquare.qrcode.core.gui.acoes.impl;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.core.gui.acoes.AcaoStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@Getter
@AllArgsConstructor
public class VoltarMenuStrategyImpl implements AcaoStrategy {

    private LojaSquare pl;

    @Override
    public void executar(Player p, Inventory inv) {
        pl.getOpenGuiPrincipal().execute(p);
    }
}
