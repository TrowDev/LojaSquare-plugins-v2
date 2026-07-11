package br.com.lojasquare.utils;

import br.com.lojasquare.LojasquareMod;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.providers.lojasquare.impl.LSProviderImpl;
import br.com.lojasquare.providers.request.IRequestProvider;
import br.com.lojasquare.providers.request.impl.RequestProviderImpl;

public class PluginLoadUtil {

    public void prepareWebServiceConnection(LojasquareMod mod) {
        SiteUtil siteUtil = new SiteUtil();
        siteUtil.setCredencial(mod.getKeyAPI());
        siteUtil.setTokenServidor(mod.getConfigManager().getString("LojaSquare.Token_Servidor"));
        siteUtil.setConnectionTimeout(mod.getConfigManager().getInt("LojaSquare.Connection_Timeout", 10000));
        siteUtil.setReadTimeout(mod.getConfigManager().getInt("LojaSquare.Read_Timeout", 10000));
        siteUtil.setDebug(mod.canDebug());
        siteUtil.setServerRequest("https://api.lojasquare.net");

        IRequestProvider requestProvider = new RequestProviderImpl(siteUtil);
        ILSProvider lsProvider = new LSProviderImpl(requestProvider, mod);

        mod.setSiteUtil(siteUtil);
        mod.setRequestProvider(requestProvider);
        mod.setLsProvider(lsProvider);
    }
}
