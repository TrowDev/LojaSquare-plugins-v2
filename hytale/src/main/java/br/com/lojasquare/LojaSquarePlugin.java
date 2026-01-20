package br.com.lojasquare;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import br.com.lojasquare.api.ProductActiveEvent;
import br.com.lojasquare.api.ProductPreActiveEvent;
import br.com.lojasquare.commands.CmdLSite;
import br.com.lojasquare.core.CheckService;
import br.com.lojasquare.core.autoconfig.CheckCreateGroupItem;
import br.com.lojasquare.core.delivery.CheckDelivery;
import br.com.lojasquare.listener.ProdutoListener;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.providers.lojasquare.impl.LSProviderImpl;
import br.com.lojasquare.providers.request.IRequestProvider;
import br.com.lojasquare.providers.request.impl.RequestProviderImpl;
import br.com.lojasquare.utils.ConfigManager;
import br.com.lojasquare.utils.SiteUtil;
import br.com.lojasquare.utils.model.ValidaIpInfo;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * Classe principal do plugin LojaSquare para Hytale.
 * Estende JavaPlugin do Hytale Server API.
 */
public class LojaSquarePlugin extends JavaPlugin {

    private static LojaSquarePlugin instance;

    @Getter
    private ConfigManager configManager;

    @Getter
    private ConfigManager confGrupos;

    private int tempoChecarItens;
    private String servidor;
    private boolean debug;
    private boolean smartDelivery;

    @Getter
    private List<String> produtosAtivados = new ArrayList<>();

    @Getter
    private List<String> produtosConfigurados = new ArrayList<>();

    @Getter
    @Setter
    private ILSProvider lsProvider;

    @Getter
    @Setter
    private IRequestProvider requestProvider;

    @Getter
    private SiteUtil siteUtil;

    private ProdutoListener produtoListener;
    private CmdLSite cmdLSite;
    private CheckDelivery checkDelivery;

    private final Map<UUID, String> onlinePlayers = new ConcurrentHashMap<>();
    private final Map<String, UUID> playerNameToUUID = new ConcurrentHashMap<>();

    public LojaSquarePlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        super.setup();
        log("[LojaSquare] Carregando configuracoes...");
    }

    @Override
    protected void start() {
        super.start();
        try {
            defineVariaveisAmbiente();
            String keyapi = getKeyAPI();

            if (!checarServidorConfigurado()) {
                return;
            }

            log("[LojaSquare] Ativado...");
            log("Criador: Trow");

            carregaGruposEntregaConfigurados();
            prepareWebServiceConnection(keyapi);
            checarIPCorreto(keyapi);
            registraEventosCmds();
            checagensDeInicializacao();
        } catch (Exception e) {
            e.printStackTrace();
            log("[LojaSquare] Erro ao iniciar: " + e.getMessage());
        }
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        log("[LojaSquare] Desativado...");
        if (checkDelivery != null) {
            checkDelivery.shutdown();
        }
    }

    private void defineVariaveisAmbiente() {
        configManager = new ConfigManager("config", this);
        debug = configManager.getBoolean("LojaSquare.Debug", true);
        servidor = configManager.getString("LojaSquare.Servidor", null);
        smartDelivery = configManager.getBoolean("LojaSquare.Smart_Delivery", true);
        tempoChecarItens = configManager.getInt("Config.Tempo_Checar_Compras", 60);
    }

    private boolean checarServidorConfigurado() {
        if (servidor == null || servidor.equalsIgnoreCase("Nome-Do-Servidor")) {
            log("[LojaSquare] Configure o nome do servidor na config.json");
            return false;
        }
        return true;
    }

    private void carregaGruposEntregaConfigurados() {
        confGrupos = new ConfigManager("produtos", this);
        Set<String> grupos = confGrupos.getKeys("Grupos");
        if (grupos == null || grupos.isEmpty())
            return;

        for (String v : grupos) {
            produtosConfigurados.add(v);
            if (confGrupos.getBoolean("Grupos." + v + ".Ativado")) {
                produtosAtivados.add(v);
                log("[LojaSquare] Grupo carregado: " + v);
            }
        }
    }

    private void prepareWebServiceConnection(String keyapi) {
        siteUtil = new SiteUtil();
        siteUtil.setCredencial(keyapi);
        siteUtil.setTokenServidor(getMsg("LojaSquare.Token_Servidor"));
        siteUtil.setConnectionTimeout(configManager.getInt("LojaSquare.Connection_Timeout", 1500));
        siteUtil.setReadTimeout(configManager.getInt("LojaSquare.Read_Timeout", 3000));
        siteUtil.setDebug(debug);
        siteUtil.setServerRequest("https://api.lojasquare.net");

        requestProvider = new RequestProviderImpl(siteUtil);
        lsProvider = new LSProviderImpl(requestProvider, this);
    }

    private void checarIPCorreto(String nome) {
        CompletableFuture.runAsync(() -> {
            ValidaIpInfo result = lsProvider.getIpMaquina();
            if (Objects.isNull(result) || !result.isSucesso()) {
                log("[LojaSquare] IP invalido: " + (result != null ? result.getIp() : "Erro de conexao"));
                return;
            }
            siteUtil.setIpMaquina(result.getIp());
            log("[LojaSquare] IP validado!");
        });
    }

    private void registraEventosCmds() {
        produtoListener = new ProdutoListener(this, lsProvider);
        cmdLSite = new CmdLSite(this, lsProvider);
    }

    private void checagensDeInicializacao() {
        List<CheckService> checkServices = new ArrayList<>();
        checkServices.add(new CheckCreateGroupItem(this, lsProvider));
        checkDelivery = new CheckDelivery(this, lsProvider);
        checkServices.add(checkDelivery);
        checkServices.forEach(CheckService::execute);
    }

    public Path getDataFolder() {
        return java.nio.file.Paths.get("mods", "LojaSquare");
    }

    public String getKeyAPI() {
        return getMsg("LojaSquare.SECRET_API");
    }

    public String getMsg(String path) {
        try {
            String msg = configManager.getString(path);
            return msg != null ? msg.replace("&", "§") : "";
        } catch (Exception e) {
            return "";
        }
    }

    public boolean produtoAtivado(String grupo) {
        return produtosAtivados.contains(grupo);
    }

    public int getTempoChecarItens() {
        return tempoChecarItens < 20 ? 20 : tempoChecarItens;
    }

    public boolean doSmartDelivery() {
        return smartDelivery;
    }

    public boolean canDebug() {
        return debug;
    }

    public String getServidor() {
        return servidor;
    }

    public static LojaSquarePlugin getInstance() {
        return instance;
    }

    public void log(String message) {
        System.out.println("[LojaSquare] " + message.replaceAll("§[0-9a-fk-or]", ""));
    }

    public void printDebug(String message) {
        if (debug)
            log(message);
    }

    public UUID getPlayerUUID(String playerName) {
        return playerNameToUUID.get(playerName.toLowerCase());
    }

    public String getPlayerName(UUID playerUUID) {
        return onlinePlayers.get(playerUUID);
    }

    public boolean isPlayerOnline(String playerName) {
        return playerNameToUUID.containsKey(playerName.toLowerCase());
    }

    public boolean isPlayerInventoryEmpty(UUID playerUUID) {
        return true;
    }

    public void sendMessageToPlayer(UUID playerUUID, String message) {
        log("[Para " + playerUUID + "] " + message);
    }

    public void executeCommand(String command) {
        log("[CMD] " + command);
    }

    public void handleProductPreActiveEvent(ProductPreActiveEvent event) {
        if (produtoListener != null)
            produtoListener.handlePreActive(event);
    }

    public void handleProductActiveEvent(ProductActiveEvent event) {
        if (produtoListener != null)
            produtoListener.handleActiveDelivery(event);
    }

    public boolean handleLSiteCommand(String senderName, UUID senderUUID, boolean isPlayer, String[] args) {
        return cmdLSite != null && cmdLSite.onCommand(senderName, senderUUID, isPlayer, args);
    }

    public void addOnlinePlayer(UUID uuid, String name) {
        onlinePlayers.put(uuid, name);
        playerNameToUUID.put(name.toLowerCase(), uuid);
    }

    public void removeOnlinePlayer(UUID uuid) {
        String name = onlinePlayers.remove(uuid);
        if (name != null)
            playerNameToUUID.remove(name.toLowerCase());
    }
}
