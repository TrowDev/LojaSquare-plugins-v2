package br.com.lojasquare.providers.lojasquare;

import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;

import java.util.List;

public interface ILSProvider {
    List<ItemInfo> getTodasEntregas(LSEntregaStatus status);
    List<ProdutoInfo> getTodosProdutosDaLoja();
    boolean updateDelivery(ItemInfo ii);
    boolean activateAccount(String codigo);
}
