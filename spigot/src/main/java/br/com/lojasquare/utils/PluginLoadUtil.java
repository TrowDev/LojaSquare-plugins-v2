package br.com.lojasquare.utils;

import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.providers.lojasquare.impl.LSProviderImpl;
import br.com.lojasquare.providers.request.IRequestProvider;
import br.com.lojasquare.providers.request.impl.RequestProviderImpl;
import org.bukkit.command.ConsoleCommandSender;

import br.com.lojasquare.LojaSquare;

public class PluginLoadUtil {
	
	public void prepareWebServiceConnection(ConsoleCommandSender b, String keyapi, ILSProvider provider, IRequestProvider requestProvider, SiteUtil ls, LojaSquare pl) {
		b.sendMessage("§3[LojaSquare] §bDefinindo variaveis de conexao com o site §dLojaSquare§b...");
		ls = new SiteUtil();
		ls.setCredencial(keyapi);
		ls.setTokenServidor(pl.getMsg("LojaSquare.Token_Servidor"));
		ls.setConnectionTimeout(pl.getConfig().getInt("LojaSquare.Connection_Timeout",1500));
		ls.setReadTimeout(pl.getConfig().getInt("LojaSquare.Read_Timeout",3000));
		ls.setDebug(pl.canDebug());
		ls.setServerRequest("https://api.lojasquare.net");
		requestProvider = new RequestProviderImpl(ls);
		provider = new LSProviderImpl(requestProvider, pl);
		pl.setLsProvider(provider);
		pl.setSiteUtil(ls);
		//ls.setServerRequest("https://d2d67503f45c.ngrok.io");
		b.sendMessage("§3[LojaSquare] §bVariaveis definidas!");
	}
	
}
