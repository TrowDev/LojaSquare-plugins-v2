package br.com.lojasquare.listener;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import br.com.lojasquare.LojaSquarePlugin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.lojasquare.api.ProductActiveEvent;
import br.com.lojasquare.api.ProductPreActiveEvent;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.model.ItemInfo;

/**
 * Listener de eventos de produto.
 * Equivalente ao ProdutoListener do Spigot, adaptado para Hytale.
 * 
 * Responsabilidades:
 * - Processar ProductPreActiveEvent (atualizar API)
 * - Processar ProductActiveEvent (executar comandos de entrega)
 * - Substituir placeholders nos comandos
 * - Enviar mensagens ao jogador
 */
@AllArgsConstructor
public class ProdutoListener {

    @Getter
    private final LojaSquarePlugin plugin;

    @Getter
    private final ILSProvider lsProvider;

    /**
     * Processa evento de pré-ativação.
     * Marca a entrega como concluída na API antes de executar comandos.
     */
    public void handlePreActive(ProductPreActiveEvent event) {
        plugin.printDebug("§3[LojaSquare] §bpreActive");

        if (event.isCancelled())
            return;

        // Executa assincronamente para não bloquear
        CompletableFuture.runAsync(() -> {
            plugin.printDebug("§3[LojaSquare] §bAntes update delivery.");

            ItemInfo ii = event.getItemInfo();

            if (lsProvider.updateDelivery(ii)) {
                plugin.printDebug("§6[LojaSquare] §ePreparando entrega do produto: §7" + ii.toString());

                // Dispara evento de ativação na thread principal
                ProductActiveEvent activeEvent = new ProductActiveEvent(
                        event.getPlayerUUID(),
                        event.getPlayerName(),
                        ii);
                plugin.handleProductActiveEvent(activeEvent);
            } else {
                plugin.log("§4[LojaSquare] §cNao foi possivel atualizar o status da compra: §a" + ii.toString() +
                        "§c para: 'Entregue'. Portanto, a entrega nao foi feita!");
            }
        });
    }

    /**
     * Processa evento de ativação.
     * Executa os comandos de entrega configurados para o grupo do produto.
     */
    public void handleActiveDelivery(ProductActiveEvent event) {
        ItemInfo ii = event.getItemInfo();

        if (event.isCancelled()) {
            plugin.log("§4[LojaSquare] §cAtivacao da compra: §a" + ii.toString() +
                    "§c foi cancelada por meio do evento §aProductActiveEvent§c" +
                    ", mas ja foi marcado no site com status 'Entregue'.");
            return;
        }

        // Busca configurações do grupo
        List<String> listCmds = plugin.getConfGrupos().getStringList("Grupos." + ii.getGrupo() + ".Cmds_A_Executar");
        boolean isMoney = plugin.getConfGrupos().getBoolean("Grupos." + ii.getGrupo() + ".Money");
        boolean smartDelivery = plugin.doSmartDelivery();

        double qntMoney = 0;
        if (isMoney) {
            qntMoney = plugin.getConfGrupos().getDouble("Grupos." + ii.getGrupo() + ".Quantidade_De_Money");
        }

        if (smartDelivery) {
            qntMoney *= ii.getQuantidade();
        }

        int qntMoneyInteiro = (int) qntMoney;

        // Executa comandos
        if (smartDelivery) {
            // Smart Delivery: executa 1x com quantidade total
            for (String cmd : listCmds) {
                dispatchCommandDelivery(ii, qntMoneyInteiro, cmd);
            }
        } else {
            // Normal: executa N vezes
            for (int i = 1; i <= ii.getQuantidade(); i++) {
                for (String cmd : listCmds) {
                    dispatchCommandDelivery(ii, qntMoneyInteiro, cmd);
                }
            }
        }

        // Envia mensagem ao player
        sendMsgToPlayerOnActiveProducts(ii, event.getPlayerUUID());

        plugin.printDebug("§3[LojaSquare] §bEntrega do produto §a" + ii.toString() + "§b concluida com sucesso!");
        plugin.printDebug("");
    }

    /**
     * Executa um comando de entrega substituindo placeholders.
     */
    private void dispatchCommandDelivery(ItemInfo ii, int qntMoneyInteiro, String cmd) {
        try {
            cmd = replaceString(ii, cmd, qntMoneyInteiro, qntMoneyInteiro);
            plugin.executeCommand(cmd);
        } catch (Exception e) {
            plugin.log("§4[LojaSquare] §cErro ao executar o cmd §a" + cmd +
                    "§c da entrega com ID: §a" + ii.getEntregaID() +
                    "§c e codigo de transacao: §a" + ii.getCodigo() +
                    "§c. Erro: §a" + e.getMessage());
            if (plugin.canDebug()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Substitui placeholders no comando/mensagem.
     * 
     * Placeholders:
     * - @player: Nome do jogador
     * - @produto: Nome do produto
     * - @grupo: Grupo do produto
     * - @qnt: Quantidade
     * - @dias: Dias do produto
     * - @money: Valor monetário (double)
     * - @moneyInteiro: Valor monetário (int)
     * - @cupom: Código do cupom
     */
    public String replaceString(ItemInfo ii, String text, int qntMoneyInteiro, double qntMoney) {
        text = text.replace("@moneyInteiro", (qntMoneyInteiro > 0 ? qntMoneyInteiro : "") + "")
                .replace("@money", (qntMoney > 0 ? "" + qntMoney : ""))
                .replace("@grupo", ii.getGrupo());
        text = text.replace("@dias", ii.getDias() + "")
                .replace("@player", ii.getPlayer());
        text = text.replace("@qnt", ii.getQuantidade() + "")
                .replace("@produto", ii.getProduto());

        if (Objects.nonNull(ii.getCupom())) {
            text = text.replace("@cupom", ii.getCupom());
        } else {
            text = text.replace("@cupom", "SEM CUPOM");
        }

        return text;
    }

    /**
     * Envia mensagem configurada ao jogador após ativação do produto.
     */
    private void sendMsgToPlayerOnActiveProducts(ItemInfo ii, java.util.UUID playerUUID) {
        if (Objects.isNull(playerUUID))
            return;

        if (plugin.getConfGrupos().getBoolean("Grupos." + ii.getGrupo() + ".Enviar_Mensagem", false)) {
            List<String> messages = plugin.getConfGrupos().getStringList(
                    "Grupos." + ii.getGrupo() + ".Mensagem_Receber_Ao_Ativar_Produto");

            for (String msg : messages) {
                msg = msg.replace("&", "§");
                msg = replaceString(ii, msg, 0, 0);
                plugin.sendMessageToPlayer(playerUUID, msg);
            }
        }
    }
}
