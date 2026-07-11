package br.com.lojasquare.core.delivery;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import br.com.lojasquare.LojasquareMod;
import br.com.lojasquare.api.ProductPreActiveCallback;
import br.com.lojasquare.core.CheckService;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class CheckDelivery implements CheckService {

    private final LojasquareMod mod;
    private final ILSProvider lsProvider;
    private int tickCount;
    private boolean running;

    public CheckDelivery(LojasquareMod mod, ILSProvider lsProvider) {
        this.mod = mod;
        this.lsProvider = lsProvider;
        this.tickCount = 0;
        this.running = false;
    }

    @Override
    public void execute() {
        mod.log("[LojaSquare] Iniciando checagem automatica de entregas...");
        mod.log("[LojaSquare] Tempo de checagem a cada " + mod.getTempoChecarItens() + " segundos.");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCount++;
            int intervalTicks = 20 * mod.getTempoChecarItens();
            if (tickCount % intervalTicks == 0 && !running) {
                checkDeliveries(server);
            }
        });
    }

    private void checkDeliveries(MinecraftServer server) {
        running = true;

        CompletableFuture.supplyAsync(() -> {
            try {
                return lsProvider.getTodasEntregas(LSEntregaStatus.PENDENTE);
            } catch (Exception e) {
                mod.log("[LojaSquare] Erro ao buscar entregas: " + e.getMessage());
                if (mod.canDebug()) e.printStackTrace();
                return null;
            }
        }).thenAcceptAsync(itens -> {
            try {
                if (itens != null && !itens.isEmpty()) {
                    mod.printDebug("[LojaSquare] Itens Size: " + itens.size());
                    for (ItemInfo item : itens) {
                        if (!validaRegrasAntesEntregarItem(item, server)) continue;

                        ServerPlayerEntity player = server.getPlayerManager().getPlayer(item.getPlayer());
                        callEventoEntregarItem(item, player);
                    }
                }
            } finally {
                running = false;
            }
        }, server);
    }

    private void callEventoEntregarItem(ItemInfo item, ServerPlayerEntity player) {
        mod.printDebug("[LojaSquare] Pre Product Active Event");
        ProductPreActiveCallback.EVENT.invoker().onPreActive(player, item);
    }

    private boolean validaRegrasAntesEntregarItem(ItemInfo item, MinecraftServer server) {
        if (Objects.isNull(item)) return false;
        if (item.getStatusID() == 2) return false;

        String servidor = mod.getServidor();

        if (!checaServidorCorretoEntregarItem(item, servidor)) return false;

        ServerPlayerEntity player = server.getPlayerManager().getPlayer(item.getPlayer());

        if (!checaItemNaConfig(item, player)) return false;
        if (!checaEntregarComPlayerOffline(item, player)) return false;
        if (!checaPlayerInvVazio(item, player)) return false;
        if (!isNickCompativelComEntrega(player, item)) return false;

        return true;
    }

    private boolean isNickCompativelComEntrega(PlayerEntity player, ItemInfo itemInfo) {
        String nickPlayer = Objects.isNull(player) ? null : player.getName().getString();
        mod.printDebug("[LojaSquare] Checa se nick do player e compativel para entrega. Nick esperado: "
                + itemInfo.getPlayer() + ", Nick encontrado: " + nickPlayer);
        return itemInfo.getPlayer().equalsIgnoreCase(nickPlayer);
    }

    private boolean checaPlayerInvVazio(ItemInfo item, PlayerEntity player) {
        if (!Objects.isNull(player) && mod.getConfGrupos()
                .getBoolean("Grupos." + item.getGrupo() + ".Entregar_Apenas_Com_Inventario_Vazio", false)) {
            if (!isInventoryEmpty(player)) {
                player.sendMessage(
                    net.minecraft.text.Text.literal(
                        mod.getMsg("Msg.Limpe_Seu_Inventario").replace("@grupo", item.getGrupo())
                    )
                );
                return false;
            }
        }
        return true;
    }

    private boolean checaItemNaConfig(ItemInfo item, PlayerEntity player) {
        if (!mod.produtoAtivado(item.getGrupo())) {
            mod.printDebug("[LojaSquare] Produto " + item.getGrupo() + " nao configurado!");
            if (player != null) {
                player.sendMessage(
                    net.minecraft.text.Text.literal(
                        mod.getMsg("Msg.Produto_Nao_Configurado").replace("@grupo", item.getGrupo())
                    )
                );
            }
            return false;
        }
        return true;
    }

    private boolean checaEntregarComPlayerOffline(ItemInfo item, PlayerEntity player) {
        if (player == null) {
            if (!mod.getConfGrupos().getBoolean("Grupos." + item.getGrupo() + ".Ativar_Com_Player_Offline", false)) {
                boolean disputa = item.getProduto().equalsIgnoreCase("DISPUTA")
                        && item.getGrupo().equalsIgnoreCase("DISPUTA");
                boolean resolvido = item.getProduto().equalsIgnoreCase("RESOLVIDO")
                        && item.getGrupo().equalsIgnoreCase("RESOLVIDO");
                if (!disputa && !resolvido) return false;
            }
        }
        return true;
    }

    private boolean checaServidorCorretoEntregarItem(ItemInfo item, String servidor) {
        if (!item.getSubServidor().equalsIgnoreCase(servidor)) {
            mod.printDebug("[LojaSquare] Item: " + item.getProduto()
                    + " do grupo " + item.getGrupo()
                    + " esta configurado para o servidor " + item.getSubServidor()
                    + ", porem o servidor atual e o " + servidor);
            return false;
        }
        return true;
    }

    public static boolean isInventoryEmpty(PlayerEntity player) {
        for (ItemStack item : player.getInventory().main) {
            if (!item.isEmpty()) return false;
        }
        for (ItemStack item : player.getInventory().armor) {
            if (!item.isEmpty()) return false;
        }
        return true;
    }
}
