package br.com.lojasquare.listener;

import br.com.lojasquare.LojaSquare;
import br.com.lojasquare.api.ProductActiveEvent;
import br.com.lojasquare.api.ProductPreActiveEvent;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class ProdutoListener implements Listener{
	
	@Getter private LojaSquare pl;
	@Getter private ILSProvider lsProvider;
	
	@EventHandler
	public void preActive(final ProductPreActiveEvent e){
		pl.printDebug("§3[LojaSquare] §bpreActive");
		if(e.isCancelled()) return;

	}
	
}
