package br.com.lojasquare.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import br.com.lojasquare.utils.model.ItemInfo;

public class ProductActiveEvent extends Event implements Cancellable{
	
	private static final HandlerList handlers = new HandlerList();
	private Player p;
	private ItemInfo ii;
	private boolean cancelado;
	
	public ProductActiveEvent(Player player,ItemInfo item){
		p=player;
		ii=item;
	}
	
	public ItemInfo getItemInfo(){
		return ii;
	}
	
	public boolean isCancelled(){
		return cancelado;
	}
	
	public void setCancelled(boolean b){
		cancelado=b;
	}
	
	public Player getPlayer(){
		return p;
	}
	
	public HandlerList getHandlers(){
		return handlers;
	}	
	
	public static HandlerList getHandlerList(){
		return handlers;
	}
	
}
