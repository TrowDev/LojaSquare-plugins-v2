package br.com.lojasquare;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.lojasquare.command.LojasquareCommands;
import br.com.lojasquare.core.CheckService;
import br.com.lojasquare.core.autoconfig.CheckCreateGroupItem;
import br.com.lojasquare.core.delivery.CheckDelivery;
import br.com.lojasquare.listener.DeliveryListener;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.providers.request.IRequestProvider;
import br.com.lojasquare.utils.ConfigManager;
import br.com.lojasquare.utils.PluginLoadUtil;
import br.com.lojasquare.utils.SiteUtil;
import br.com.lojasquare.utils.model.ValidaIpInfo;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class LojasquareMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("lojasquare");

    private static LojasquareMod instance;

    private MinecraftServer server;
    private ConfigManager configManager;
    private ConfigManager confGrupos;
    private SiteUtil siteUtil;
    private ILSProvider lsProvider;
    private IRequestProvider requestProvider;
    private DeliveryListener deliveryListener;

    private final List<String> produtosAtivados = new ArrayList<>();
    private final List<String> produtosConfigurados = new ArrayList<>();

    private int tempoChecarItens;
    private String servidor;
    private boolean debug;
    private boolean smartDelivery;

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("[LojaSquare] Inicializando...");

        Path configDir = Path.of("config", "lojasquare");

        configManager = new ConfigManager("config", configDir);
        definiVariaveisAmbiente();

        if (!checarServidorConfigurado()) {
            return;
        }

        LOGGER.info("[LojaSquare] Ativado...");
        LOGGER.info("Criador: Trow");

        carregaGruposEntregaConfigurados();

        PluginLoadUtil plu = new PluginLoadUtil();
        plu.prepareWebServiceConnection(this);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            new LojasquareCommands(this, lsProvider).register(dispatcher, registryAccess, environment);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
    }

    private void onServerStarted(MinecraftServer server) {
        this.server = server;
        LOGGER.info("[LojaSquare] Servidor iniciado, validando IP e iniciando servicos...");

        checarIPCorreto();
        checagensDeInicializacao();

        deliveryListener = new DeliveryListener(this, lsProvider);
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("[LojaSquare] Desativado...");
        LOGGER.info("Criador: Trow");
        LOGGER.info("Agradeco por usar meu(s) plugin(s)");
    }

    private void definiVariaveisAmbiente() {
        debug = configManager.getBoolean("LojaSquare.Debug", true);
        servidor = configManager.getString("LojaSquare.Servidor", null);
        smartDelivery = configManager.getBoolean("LojaSquare.Smart_Delivery", true);
        tempoChecarItens = configManager.getInt("Config.Tempo_Checar_Compras", 60);
    }

    private boolean checarServidorConfigurado() {
        if (servidor == null || servidor.equalsIgnoreCase("Nome-Do-Servidor")) {
            LOGGER.info("[LojaSquare] Desativando...");
            LOGGER.info("[LojaSquare] Para que o plugin seja ativado com sucesso, e necessario configurar o nome do seu servidor na config.yml");
            LOGGER.info("[LojaSquare] Atualmente o nome do servidor esta definido como: " + (servidor == null ? "NAO DEFINIDO" : servidor));
            return false;
        }
        return true;
    }

    private void carregaGruposEntregaConfigurados() {
        confGrupos = new ConfigManager("produtos", Path.of("config", "lojasquare"));

        if (confGrupos.getKeys("Grupos").isEmpty()) {
            LOGGER.info("[LojaSquare] Nenhum grupo configurado encontrado.");
            return;
        }

        LOGGER.info("[LojaSquare] Iniciando o carregamento dos nomes dos grupos de itens para serem entregues...");

        for (String v : confGrupos.getKeys("Grupos")) {
            produtosConfigurados.add(v);
            if (!confGrupos.getBoolean("Grupos." + v + ".Ativado")) {
                LOGGER.info("[LojaSquare] O grupo " + v + " nao esta ativado nas configuracoes.");
                continue;
            }
            produtosAtivados.add(v);
            LOGGER.info("[LojaSquare] Grupo carregado: " + v);
        }

        LOGGER.info("[LojaSquare] Grupos de entregas foram carregados com sucesso!");
    }

    private void checarIPCorreto() {
        CompletableFuture.runAsync(() -> {
            ValidaIpInfo result = lsProvider.getIpMaquina();
            if (Objects.isNull(result) || !result.isSucesso()) {
                LOGGER.info("[LojaSquare] Desativado...");
                LOGGER.info("Criador: Trow");
                LOGGER.info("Motivo: " + (result != null ? result.getIp() : "Erro de conexao"));
                LOGGER.info("Key-API: " + getKeyAPI());
                LOGGER.info("Para atualizar o IP, acesse: https://painel.lojasquare.net/pages/config/site e clique em 'Ativacao Automatica'");
                return;
            }
            siteUtil.setIpMaquina(result.getIp());
            LOGGER.info("[LojaSquare] IP da maquina validado!");
        });
    }

    private void checagensDeInicializacao() {
        List<CheckService> checkServices = new ArrayList<>();
        checkServices.add(new CheckCreateGroupItem(this, lsProvider));
        checkServices.add(new CheckDelivery(this, lsProvider));

        checkServices.forEach(CheckService::execute);
    }

    public MinecraftServer getServer() {
        return server;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ConfigManager getConfGrupos() {
        return confGrupos;
    }

    public SiteUtil getSiteUtil() {
        return siteUtil;
    }

    public void setSiteUtil(SiteUtil siteUtil) {
        this.siteUtil = siteUtil;
    }

    public ILSProvider getLsProvider() {
        return lsProvider;
    }

    public void setLsProvider(ILSProvider lsProvider) {
        this.lsProvider = lsProvider;
    }

    public IRequestProvider getRequestProvider() {
        return requestProvider;
    }

    public void setRequestProvider(IRequestProvider requestProvider) {
        this.requestProvider = requestProvider;
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

    public String getKeyAPI() {
        return getMsg("LojaSquare.SECRET_API");
    }

    public boolean produtoAtivado(String grupo) {
        return produtosAtivados.contains(grupo);
    }

    public List<String> getProdutosAtivados() {
        return produtosAtivados;
    }

    public List<String> getProdutosConfigurados() {
        return produtosConfigurados;
    }

    public String getMsg(String path) {
        try {
            String msg = configManager.getString(path);
            return msg != null ? msg.replace("&", "§") : "";
        } catch (Exception e) {
            LOGGER.warn("[LojaSquare] Linha nao encontrada na config: " + path);
            return "";
        }
    }

    public static LojasquareMod getInstance() {
        return instance;
    }

    public void log(String message) {
        LOGGER.info(message.replaceAll("§[0-9a-fk-or]", ""));
    }

    public void printDebug(String message) {
        if (debug) {
            LOGGER.info(message.replaceAll("§[0-9a-fk-or]", ""));
            if (server != null) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (player.hasPermissionLevel(2)) {
                        player.sendMessage(net.minecraft.text.Text.literal(message));
                    }
                }
            }
        }
    }
}
