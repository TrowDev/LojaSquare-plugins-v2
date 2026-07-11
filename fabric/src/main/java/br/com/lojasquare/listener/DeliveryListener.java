package br.com.lojasquare.listener;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import br.com.lojasquare.LojasquareMod;
import br.com.lojasquare.api.ProductActiveCallback;
import br.com.lojasquare.api.ProductPreActiveCallback;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.model.ItemInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class DeliveryListener {

    private final LojasquareMod mod;
    private final ILSProvider lsProvider;

    public DeliveryListener(LojasquareMod mod, ILSProvider lsProvider) {
        this.mod = mod;
        this.lsProvider = lsProvider;

        ProductPreActiveCallback.EVENT.register(this::preActive);
        ProductActiveCallback.EVENT.register(this::activeDelivery);
    }

    private ActionResult preActive(ServerPlayerEntity player, ItemInfo item) {
        mod.printDebug("[LojaSquare] preActive");

        CompletableFuture.runAsync(() -> {
            mod.printDebug("[LojaSquare] Antes update delivery.");

            if (lsProvider.updateDelivery(item)) {
                mod.printDebug("[LojaSquare] Preparando entrega do produto: " + item.toString());

                MinecraftServer server = mod.getServer();
                if (server != null) {
                    server.execute(() -> {
                        ProductActiveCallback.EVENT.invoker().onActive(player, item);
                    });
                }
            } else {
                mod.log("[LojaSquare] Nao foi possivel atualizar o status da compra: " + item.toString()
                        + " para 'Entregue'. Portanto, a entrega nao foi feita!");
            }
        });

        return ActionResult.PASS;
    }

    private ActionResult activeDelivery(ServerPlayerEntity player, ItemInfo item) {
        if (item == null) return ActionResult.PASS;

        List<String> listCmds = mod.getConfGrupos().getStringList("Grupos." + item.getGrupo() + ".Cmds_A_Executar");
        boolean isMoney = mod.getConfGrupos().getBoolean("Grupos." + item.getGrupo() + ".Money");
        boolean smartDelivery = mod.doSmartDelivery();

        double qntMoney = 0;
        if (isMoney) {
            qntMoney = mod.getConfGrupos().getDouble("Grupos." + item.getGrupo() + ".Quantidade_De_Money");
        }

        if (smartDelivery) {
            qntMoney *= item.getQuantidade();
        }

        int qntMoneyInteiro = (int) qntMoney;

        if (smartDelivery) {
            for (String cmd : listCmds) {
                dispatchCommandDelivery(item, qntMoneyInteiro, cmd);
            }
        } else {
            for (int i = 1; i <= item.getQuantidade(); i++) {
                for (String cmd : listCmds) {
                    dispatchCommandDelivery(item, qntMoneyInteiro, cmd);
                }
            }
        }

        sendMsgToPlayerOnActiveProducts(item, player);

        mod.printDebug("[LojaSquare] Entrega do produto " + item.toString() + " concluida com sucesso!");
        return ActionResult.PASS;
    }

    private void dispatchCommandDelivery(ItemInfo item, int qntMoneyInteiro, String cmd) {
        try {
            cmd = replaceString(item, cmd, qntMoneyInteiro, qntMoneyInteiro);
            MinecraftServer server = mod.getServer();
            if (server != null) {
                ServerCommandSource source = server.getCommandSource();
                server.getCommandManager().executeWithPrefix(source, cmd);
            }
        } catch (Exception e) {
            mod.log("[LojaSquare] Erro ao executar o cmd " + cmd
                    + " da entrega com ID: " + item.getEntregaID()
                    + " e codigo de transacao: " + item.getCodigo()
                    + ". Erro: " + e.getMessage());
            if (mod.canDebug()) {
                e.printStackTrace();
            }
        }
    }

    public String replaceString(ItemInfo item, String text, int qntMoneyInteiro, double qntMoney) {
        text = text.replace("@moneyInteiro", (qntMoneyInteiro > 0 ? qntMoneyInteiro : "") + "")
                .replace("@money", (qntMoney > 0 ? "" + qntMoney : ""))
                .replace("@grupo", item.getGrupo());
        text = text.replace("@dias", item.getDias() + "")
                .replace("@player", item.getPlayer());
        text = text.replace("@qnt", item.getQuantidade() + "")
                .replace("@produto", item.getProduto());

        if (Objects.nonNull(item.getCupom())) {
            text = text.replace("@cupom", item.getCupom());
        } else {
            text = text.replace("@cupom", "SEM CUPOM");
        }

        return text;
    }

    private void sendMsgToPlayerOnActiveProducts(ItemInfo item, ServerPlayerEntity player) {
        if (Objects.isNull(player)) return;

        if (mod.getConfGrupos().getBoolean("Grupos." + item.getGrupo() + ".Enviar_Mensagem", false)) {
            List<String> messages = mod.getConfGrupos().getStringList(
                    "Grupos." + item.getGrupo() + ".Mensagem_Receber_Ao_Ativar_Produto");

            for (String msg : messages) {
                msg = msg.replace("&", "§");
                msg = replaceString(item, msg, 0, 0);
                player.sendMessage(Text.literal(msg));
            }
        }
    }
}
