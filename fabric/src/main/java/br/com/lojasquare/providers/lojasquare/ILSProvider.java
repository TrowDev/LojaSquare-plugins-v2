package br.com.lojasquare.providers.lojasquare;

import java.util.List;

import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;
import br.com.lojasquare.utils.model.ValidaIpInfo;

public interface ILSProvider {
    List<ItemInfo> getTodasEntregas(LSEntregaStatus status);
    List<ProdutoInfo> getTodosProdutosDaLoja();
    boolean updateDelivery(ItemInfo ii);
    boolean activateAccount(String codigo, String usuario);
    ValidaIpInfo getIpMaquina();
}
