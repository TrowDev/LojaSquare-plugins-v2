package br.com.lojasquare.core.delivery;

import br.com.lojasquare.LojaSquarePlugin;
import br.com.lojasquare.api.ProductPreActiveEvent;
import br.com.lojasquare.core.CheckService;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class CheckDelivery implements CheckService {

    @Getter
    private final LojaSquarePlugin plugin;
    
    @Getter
    private final ILSProvider lsProvider;
    
    private ScheduledExecutorService scheduler;

    public CheckDelivery(LojaSquarePlugin plugin, ILSProvider lsProvider) {
        this.plugin = plugin;
        this.lsProvider = lsProvider;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void execute() {
        plugin.log("[LojaSquare] Iniciando checagem automatica de entregas...");
        plugin.log("[LojaSquare] Tempo de checagem a cada " + plugin.getTempoChecarItens() + " segundos.");
        
        scheduler.scheduleAtFixedRate(
            this::checkDeliveries,
            10,
            plugin.getTempoChecarItens(),
            TimeUnit.SECONDS
        );
    }

    private void checkDeliveries() {
        try {
            List<ItemInfo> itens = lsProvider.getTodasEntregas(LSEntregaStatus.PENDENTE);
            
            plugin.printDebug("[LojaSquare] Itens Size: " + itens.size());
            
            if (itens != null && !itens.isEmpty()) {
                for (ItemInfo item : itens) {
                    if (!validaRegrasAntesEntregarItem(item)) continue;
                    
                    UUID playerUUID = plugin.getPlayerUUID(item.getPlayer());
                    callEventoEntregarItem(item, playerUUID, item.getPlayer());
                }
            }
        } catch (Exception e) {
            plugin.log("[LojaSquare] Erro ao verificar entregas: " + e.getMessage());
            if (plugin.canDebug()) {
                e.printStackTrace();
            }
        }
    }

    private void callEventoEntregarItem(ItemInfo item, UUID playerUUID, String playerName) {
        plugin.printDebug("[LojaSquare] Pre Product Active Event");
        
        ProductPreActiveEvent event = new ProductPreActiveEvent(playerUUID, playerName, item);
        plugin.handleProductPreActiveEvent(event);
    }

    private boolean validaRegrasAntesEntregarItem(ItemInfo item) {
        if (Objects.isNull(item)) return false;
        if (item.getStatusID() == 2) return false;
        
        String servidor = plugin.getServidor();
        
        if (!checaServidorCorretoEntregarItem(item, servidor)) return false;
        
        UUID playerUUID = plugin.getPlayerUUID(item.getPlayer());
        if (!checaItemNaConfig(item, playerUUID)) return false;
        if (!checaEntregarComPlayerOffline(item, playerUUID)) return false;
        if (!checaPlayerInvVazio(item, playerUUID)) return false;
        
        if (playerUUID != null) {
            String playerName = plugin.getPlayerName(playerUUID);
            if (!isNickCompativelComEntrega(playerName, item)) return false;
        }
        
        return true;
    }

    private boolean isNickCompativelComEntrega(String playerName, ItemInfo itemInfo) {
        if (playerName == null) return true;
        return itemInfo.getPlayer().equalsIgnoreCase(playerName);
    }

    private boolean checaPlayerInvVazio(ItemInfo item, UUID playerUUID) {
        if (playerUUID != null && plugin.getConfGrupos()
                .getBoolean("Grupos." + item.getGrupo() + ".Entregar_Apenas_Com_Inventario_Vazio", false)) {
            if (!plugin.isPlayerInventoryEmpty(playerUUID)) {
                plugin.sendMessageToPlayer(playerUUID, 
                        plugin.getMsg("Msg.Limpe_Seu_Inventario").replace("@grupo", item.getGrupo()));
                return false;
            }
        }
        return true;
    }

    private boolean checaItemNaConfig(ItemInfo item, UUID playerUUID) {
        if (!plugin.produtoAtivado(item.getGrupo())) {
            plugin.printDebug("[LojaSquare] Produto " + item.getGrupo() + " nao configurado!");
            if (playerUUID != null) {
                plugin.sendMessageToPlayer(playerUUID, 
                        plugin.getMsg("Msg.Produto_Nao_Configurado").replace("@grupo", item.getGrupo()));
            }
            return false;
        }
        return true;
    }

    private boolean checaEntregarComPlayerOffline(ItemInfo item, UUID playerUUID) {
        if (playerUUID == null) {
            if (!plugin.getConfGrupos().getBoolean("Grupos." + item.getGrupo() + ".Ativar_Com_Player_Offline", false)) {
                boolean disputa = item.getProduto().equalsIgnoreCase("DISPUTA") && 
                        item.getGrupo().equalsIgnoreCase("DISPUTA");
                boolean resolvido = item.getProduto().equalsIgnoreCase("RESOLVIDO") && 
                        item.getGrupo().equalsIgnoreCase("RESOLVIDO");
                if (!disputa && !resolvido) return false;
            }
        }
        return true;
    }

    private boolean checaServidorCorretoEntregarItem(ItemInfo item, String servidor) {
        if (!item.getSubServidor().equalsIgnoreCase(servidor)) {
            plugin.printDebug("[LojaSquare] Item: " + item.getProduto() + 
                    " do grupo " + item.getGrupo() + 
                    " esta configurado para o servidor " + item.getSubServidor() + 
                    ", porem o servidor atual e o " + servidor);
            return false;
        }
        return true;
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }
}
