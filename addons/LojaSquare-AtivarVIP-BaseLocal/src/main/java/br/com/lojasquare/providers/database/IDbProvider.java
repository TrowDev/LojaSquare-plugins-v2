package br.com.lojasquare.providers.database;

import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;

import java.util.List;

public interface IDbProvider {
    List<ProdutoInfo> getTodosGruposEntrega();
    List<ItemInfo> getTodasEntregas(LSEntregaStatus status);
    boolean updateDelivery(ItemInfo ii);
}
