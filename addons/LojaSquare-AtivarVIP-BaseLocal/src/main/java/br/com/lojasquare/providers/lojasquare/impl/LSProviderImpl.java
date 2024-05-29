package br.com.lojasquare.providers.lojasquare.impl;

import br.com.lojasquare.LojaSquare;
import br.com.lojasquare.providers.database.IDbProvider;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

public class LSProviderImpl implements ILSProvider {

    @Getter private LojaSquare pl;
    @Getter private IDbProvider dbProvider;

    public LSProviderImpl(IDbProvider iDbProvider, LojaSquare pl) {
        this.dbProvider = iDbProvider;
        this.pl     = pl;
    }

    /**
     * Retorna uma lista de produtos a serem entregues (Retorno para todos os
     * players).
     *
     * @return List<ItemInfo> lista de produtos a serem entregues.
     */
    @Override
    public List<ItemInfo> getTodasEntregas(LSEntregaStatus status) {
        return getDbProvider().getTodasEntregas(status);
    }

    /**
     * Busca todos os produtos criados na loja e seus respectivos grupos
     * @return Lista de informações dos produtos criados na loja.
     */
    @Override
    public List<ProdutoInfo> getTodosProdutosDaLoja() {
        return getDbProvider().getTodosGruposEntrega();
    }

    /**
     * O metodo executa o update da entrega, informando na api que a entrega com ID
     * 'x' foi realizada.
     *
     * @param ii ItemInfo a ser entregue e atualizado
     * @return Retornara true se o update da entrega for realizado com sucesso.
     */
    @Override
    public boolean updateDelivery(ItemInfo ii) {
        if (Objects.isNull(ii)) {
            return false;
        }
        return getDbProvider().updateDelivery(ii);
    }

}
