package br.com.lojasquare.providers.lojasquare;

import java.util.List;

import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;
import br.com.lojasquare.utils.model.ValidaIpInfo;

/**
 * Interface para provider da API LojaSquare.
 * Classe agnóstica de plataforma - reutilizada do projeto Spigot.
 */
public interface ILSProvider {

    /**
     * Busca todas as entregas com o status especificado.
     * 
     * @param status Status das entregas (PENDENTE ou ENTREGUE)
     * @return Lista de ItemInfo das entregas
     */
    List<ItemInfo> getTodasEntregas(LSEntregaStatus status);

    /**
     * Busca todos os produtos cadastrados na loja.
     * 
     * @return Lista de ProdutoInfo dos produtos
     */
    List<ProdutoInfo> getTodosProdutosDaLoja();

    /**
     * Marca uma entrega como concluída na API.
     * 
     * @param ii ItemInfo da entrega
     * @return true se a atualização foi bem sucedida
     */
    boolean updateDelivery(ItemInfo ii);

    /**
     * Ativa uma conta de cliente.
     * 
     * @param codigo  Código de ativação
     * @param usuario Nome do usuário
     * @return true se a ativação foi bem sucedida
     */
    boolean activateAccount(String codigo, String usuario);

    /**
     * Valida o IP da máquina do servidor.
     * 
     * @return ValidaIpInfo com resultado da validação
     */
    ValidaIpInfo getIpMaquina();
}
