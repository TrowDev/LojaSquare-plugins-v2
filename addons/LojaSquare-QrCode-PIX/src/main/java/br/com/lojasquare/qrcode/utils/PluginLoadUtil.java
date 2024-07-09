package br.com.lojasquare.qrcode.utils;

import br.com.lojasquare.qrcode.providers.lojasquare.ILSProvider;
import br.com.lojasquare.qrcode.providers.lojasquare.impl.LSProviderImpl;
import br.com.lojasquare.qrcode.providers.request.IRequestProvider;
import br.com.lojasquare.qrcode.providers.request.impl.RequestProviderImpl;
import br.com.lojasquare.qrcode.LojaSquare;
import com.google.gson.Gson;
import org.bukkit.command.ConsoleCommandSender;

public class PluginLoadUtil {
	
	public void prepareWebServiceConnection(ConsoleCommandSender b, String keyapi, String publicApi, ILSProvider provider, IRequestProvider requestProvider, SiteUtil ls, LojaSquare pl) {
		b.sendMessage("§3[LSQrCode] §bDefinindo variaveis de conexao com o site §dLojaSquare§b...");
		ls = new SiteUtil();
		ls.setCredencial(keyapi);
		ls.setChavePublica(publicApi);
		ls.setTokenServidor(pl.getMsg("LojaSquare.Token_Servidor"));
		ls.setConnectionTimeout(pl.getConfig().getInt("LojaSquare.Connection_Timeout",5000));
		ls.setReadTimeout(pl.getConfig().getInt("LojaSquare.Read_Timeout",10000));
		ls.setDebug(pl.canDebug());
		ls.setServerRequest("https://api.lojasquare.net");
		requestProvider = new RequestProviderImpl(ls);
		provider = new LSProviderImpl(requestProvider, pl, new Gson());
		pl.setLsProvider(provider);
		pl.setSiteUtil(ls);
		//ls.setServerRequest("https://d2d67503f45c.ngrok.io");
		b.sendMessage("§3[LSQrCode] §bVariaveis definidas!");
	}
	
}
