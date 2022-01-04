package me.trow.lojasquare.utils;

import org.bukkit.command.ConsoleCommandSender;

import me.trow.lojasquare.LojaSquare;

public class PluginLoadUtil {
	
	public void prepareWebServiceConnection(ConsoleCommandSender b, String keyapi, SiteUtil ls, LojaSquare pl) {
		b.sendMessage("§3[LojaSquare] §bDefinindo variaveis de conexao com o site §dLojaSquare§b...");
		ls = new SiteUtil();
		ls.setCredencial(keyapi);
		ls.setTokenServidor(pl.getMsg("LojaSquare.Token_Servidor"));
		ls.setConnectionTimeout(pl.getConfig().getInt("LojaSquare.Connection_Timeout",1500));
		ls.setReadTimeout(pl.getConfig().getInt("LojaSquare.Read_Timeout",3000));
		ls.setDebug(pl.canDebug());
		pl.setExecUtil(new ExecutorUtil(ls));
		pl.setSiteUtil(ls);
		ls.setServerRequest("https://api.lojasquare.net");
		//ls.setServerRequest("https://d2d67503f45c.ngrok.io");
		b.sendMessage("§3[LojaSquare] §bVariaveis definidas!");
	}
	
}
