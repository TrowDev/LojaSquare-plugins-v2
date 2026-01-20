package br.com.lojasquare;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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

/**
 * Classe principal do plugin LojaSquare para Hytale.
 * Equivalente ao LojaSquare extends JavaPlugin do Spigot.
 * 
 * Responsabilidades:
 * - Inicialização e desligamento do plugin
 * - Gerenciamento de configurações
 * - Registro de comandos e eventos
 * - Integração com a API LojaSquare
 * - Gerenciamento de entregas de produtos
 */
public class LojaSquarePlugin {

    // Instância singleton
    private static LojaSquarePlugin instance;

    // Configurações
    @Getter
    private ConfigManager configManager;

    @Getter
    private ConfigManager confGrupos;

    @Getter
    private Path dataFolder;

    // Variáveis de ambiente
    private int tempoChecarItens;
    private String servidor;
    private boolean debug;
    private boolean smartDelivery;

    // Listas de produtos
    @Getter
    private List<String> produtosAtivados = new ArrayList<>();

    @Getter
    private List<String> produtosConfigurados = new ArrayList<>();

    // Providers
    @Getter
    @Setter
    private ILSProvider lsProvider;

    @Getter
    @Setter
    private IRequestProvider requestProvider;

    @Getter
    private SiteUtil siteUtil;

    // Componentes
    private ProdutoListener produtoListener;
    private CmdLSite cmdLSite;
    private CheckDelivery checkDelivery;

    // Simulação de jogadores online (em produção, usar API do Hytale)
    private final Map<UUID, String> onlinePlayers = new ConcurrentHashMap<>();
    private final Map<String, UUID> playerNameToUUID = new ConcurrentHashMap<>();

    /**
     * Construtor - define pasta de dados do plugin.
     */
    public LojaSquarePlugin() {
        instance = this;
        this.dataFolder = Paths.get("plugins", "LojaSquare");
    }

    /**
     * Método de setup (chamado na fase de configuração do Hytale).
     * Equivalente à primeira parte do onEnable() do Spigot.
     */
    public void setup() {
        log("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        log("§3[LojaSquare] §bCarregando configuracoes...");
    }

    /**
     * Método de start (chamado quando o servidor inicia).
     * Equivalente ao onEnable() do Spigot.
     */
    public void start() {
        try {
            defineVariaveisAmbiente();
            String keyapi = getKeyAPI();

            if (!checarServidorConfigurado()) {
                return;
            }

            log("§3[LojaSquare] §bAtivado...");
            log("§3Criador: §bTrow");
            log("§bDesejo a voce uma otima experiencia com o §dLojaSquare§b.");

            // Carrega grupos de produtos configurados
            carregaGruposEntregaConfigurados();

            // Prepara conexão com WebService
            prepareWebServiceConnection(keyapi);

            // Valida IP da máquina
            checarIPCorreto(keyapi);

            // Registra eventos e comandos
            registraEventosCmds();

            // Executa checagens de inicialização
            checagensDeInicializacao();

            log("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        } catch (Exception e) {
            e.printStackTrace();
            log("§4[LojaSquare] §cErro ao iniciar o plugin LojaSquare.");
            log("§4[LojaSquare] §cErro: §a" + e.getMessage());
        }
    }

    /**
     * Método de shutdown (chamado quando o servidor desliga).
     * Equivalente ao onDisable() do Spigot.
     */
    public void shutdown() {
        log("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        log("§3[LojaSquare] §bDesativado...");
        log("§3Criador: §bTrow");
        log("§bAgradeco por usar meu(s) plugin(s)");
        log("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");

        // Para o scheduler de entregas
        if (checkDelivery != null) {
            checkDelivery.shutdown();
        }
    }

    /**
     * Define variáveis de ambiente a partir da configuração.
     */
    private void defineVariaveisAmbiente() {
        // Carrega configurações
        configManager = new ConfigManager("config", this);

        debug = configManager.getBoolean("LojaSquare.Debug", true);
        servidor = configManager.getString("LojaSquare.Servidor", null);
        smartDelivery = configManager.getBoolean("LojaSquare.Smart_Delivery", true);
        tempoChecarItens = configManager.getInt("Config.Tempo_Checar_Compras", 60);
    }

    /**
     * Verifica se o servidor foi configurado.
     */
    private boolean checarServidorConfigurado() {
        if (servidor == null || servidor.equalsIgnoreCase("Nome-Do-Servidor")) {
            log("§4[LojaSquare] §cDesativando...");
            log("§4[LojaSquare] §cPara que o plugin seja ativado com sucesso, e necessario configurar o nome do seu servidor na config.json");
            log("§4[LojaSquare] §cAtualmente o nome do servidor esta definido como: §a" +
                    (servidor == null ? "§4NAO DEFINIDO" : servidor));
            log("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
            return false;
        }
        return true;
    }

    /**
     * Carrega grupos de entrega configurados no arquivo produtos.json.
     */
    private void carregaGruposEntregaConfigurados() {
        confGrupos = new ConfigManager("produtos", this);

        Set<String> grupos = confGrupos.getKeys("Grupos");
        if (grupos == null || grupos.isEmpty()) {
            log("§3[LojaSquare] §bNenhum grupo de produtos configurado ainda.");
            return;
        }

        log("§3[LojaSquare] §bIniciando o carregamento dos nomes dos grupos de itens para serem entregues...");

        for (String v : grupos) {
            produtosConfigurados.add(v);

            if (!confGrupos.getBoolean("Grupos." + v + ".Ativado")) {
                log("§4[LojaSquare] §cO grupo §a" + v + "§c nao esta ativado nas configuracoes.");
                continue;
            }

            produtosAtivados.add(v);
            log("§3[LojaSquare] §bGrupo carregado: §a" + v);
        }

        log("§3[LojaSquare] §bGrupos de entregas foram carregados com sucesso!");
    }

    /**
     * Prepara conexão com o WebService da LojaSquare.
     */
    private void prepareWebServiceConnection(String keyapi) {
        log("§3[LojaSquare] §bDefinindo variaveis de conexao com o site §dLojaSquare§b...");

        siteUtil = new SiteUtil();
        siteUtil.setCredencial(keyapi);
        siteUtil.setTokenServidor(getMsg("LojaSquare.Token_Servidor"));
        siteUtil.setConnectionTimeout(configManager.getInt("LojaSquare.Connection_Timeout", 1500));
        siteUtil.setReadTimeout(configManager.getInt("LojaSquare.Read_Timeout", 3000));
        siteUtil.setDebug(debug);
        siteUtil.setServerRequest("https://api.lojasquare.net");

        requestProvider = new RequestProviderImpl(siteUtil);
        lsProvider = new LSProviderImpl(requestProvider, this);

        log("§3[LojaSquare] §bVariaveis definidas!");
    }

    /**
     * Valida IP da máquina via API.
     */
    private void checarIPCorreto(String nome) {
        CompletableFuture.runAsync(() -> {
            ValidaIpInfo result = lsProvider.getIpMaquina();

            if (Objects.isNull(result) || !result.isSucesso()) {
                log("§3[LojaSquare] §cDesativado...");
                log("§3Criador: §3Trow");
                log("§cMotivo: " + (result != null ? result.getIp() : "Erro de conexão"));
                log("§3Key-API: §a" + nome);
                log("§ePara atualizar o IP, acesse: §ahttps://painel.lojasquare.net/pages/config/site§e e clique em '§aAtivacao Automatica§e'");
                log("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                return;
            }

            siteUtil.setIpMaquina(result.getIp());
            log("§3[LojaSquare] §bIP da maquina validado!");
        });
    }

    /**
     * Registra eventos e comandos.
     */
    private void registraEventosCmds() {
        produtoListener = new ProdutoListener(this, lsProvider);
        cmdLSite = new CmdLSite(this, lsProvider);

        // Em produção Hytale, registrar via CommandManager e EventBus
        log("§3[LojaSquare] §bComando /lsite registrado.");
    }

    /**
     * Executa serviços de checagem na inicialização.
     */
    private void checagensDeInicializacao() {
        List<CheckService> checkServices = new ArrayList<>();
        checkServices.add(new CheckCreateGroupItem(this, lsProvider));

        checkDelivery = new CheckDelivery(this, lsProvider);
        checkServices.add(checkDelivery);

        checkServices.forEach(CheckService::execute);
    }

    // ==================== MÉTODOS DE UTILIDADE ====================

    /**
     * Obtém a KEY API da configuração.
     */
    public String getKeyAPI() {
        return getMsg("LojaSquare.SECRET_API");
    }

    /**
     * Obtém uma mensagem da configuração.
     */
    public String getMsg(String path) {
        try {
            String msg = configManager.getString(path);
            return msg != null ? msg.replace("&", "§") : "";
        } catch (Exception e) {
            log("§cLinha nao encontrada na config: §a" + path);
            return "";
        }
    }

    /**
     * Verifica se um produto está ativado.
     */
    public boolean produtoAtivado(String grupo) {
        return produtosAtivados.contains(grupo);
    }

    /**
     * Obtém o tempo de checagem de itens.
     */
    public int getTempoChecarItens() {
        if (tempoChecarItens < 20)
            return 20;
        return tempoChecarItens;
    }

    /**
     * Verifica se o Smart Delivery está ativado.
     */
    public boolean doSmartDelivery() {
        return smartDelivery;
    }

    /**
     * Verifica se o debug está ativado.
     */
    public boolean canDebug() {
        return debug;
    }

    /**
     * Obtém o nome do servidor.
     */
    public String getServidor() {
        return servidor;
    }

    /**
     * Loga uma mensagem no console.
     */
    public void log(String message) {
        // Remove códigos de cor para log simples
        String cleanMessage = message.replaceAll("§[0-9a-fk-or]", "");
        System.out.println("[LojaSquare] " + cleanMessage);
    }

    /**
     * Imprime mensagem de debug.
     */
    public void printDebug(String message) {
        if (debug) {
            log(message);
        }
    }

    /**
     * Obtém a instância singleton do plugin.
     */
    public static LojaSquarePlugin getInstance() {
        return instance;
    }

    // ==================== MÉTODOS DE INTEGRAÇÃO COM HYTALE ====================

    /**
     * Busca UUID de um jogador pelo nome.
     * Em produção Hytale, usar API de jogadores.
     */
    public UUID getPlayerUUID(String playerName) {
        return playerNameToUUID.get(playerName.toLowerCase());
    }

    /**
     * Busca nome de um jogador pelo UUID.
     * Em produção Hytale, usar API de jogadores.
     */
    public String getPlayerName(UUID playerUUID) {
        return onlinePlayers.get(playerUUID);
    }

    /**
     * Verifica se jogador está online.
     */
    public boolean isPlayerOnline(String playerName) {
        return playerNameToUUID.containsKey(playerName.toLowerCase());
    }

    /**
     * Verifica se inventário do jogador está vazio.
     * Em produção Hytale, usar API de inventário.
     */
    public boolean isPlayerInventoryEmpty(UUID playerUUID) {
        // TODO: Implementar usando API do Hytale
        return true;
    }

    /**
     * Envia mensagem a um jogador.
     * Em produção Hytale, usar API de mensagens.
     */
    public void sendMessageToPlayer(UUID playerUUID, String message) {
        // TODO: Implementar usando API do Hytale
        log("[Para " + playerUUID + "] " + message);
    }

    /**
     * Executa um comando no servidor.
     * Em produção Hytale, usar CommandManager.
     */
    public void executeCommand(String command) {
        // TODO: Implementar usando API do Hytale
        log("[CMD] " + command);
    }

    /**
     * Processa evento de pré-ativação de produto.
     */
    public void handleProductPreActiveEvent(ProductPreActiveEvent event) {
        if (produtoListener != null) {
            produtoListener.handlePreActive(event);
        }
    }

    /**
     * Processa evento de ativação de produto.
     */
    public void handleProductActiveEvent(ProductActiveEvent event) {
        if (produtoListener != null) {
            produtoListener.handleActiveDelivery(event);
        }
    }

    /**
     * Processa comando /lsite.
     */
    public boolean handleLSiteCommand(String senderName, UUID senderUUID, boolean isPlayer, String[] args) {
        if (cmdLSite != null) {
            return cmdLSite.onCommand(senderName, senderUUID, isPlayer, args);
        }
        return false;
    }

    // ==================== MÉTODOS PARA SIMULAR JOGADORES (TESTES)
    // ====================

    /**
     * Adiciona um jogador à lista de online (para testes).
     */
    public void addOnlinePlayer(UUID uuid, String name) {
        onlinePlayers.put(uuid, name);
        playerNameToUUID.put(name.toLowerCase(), uuid);
    }

    /**
     * Remove um jogador da lista de online (para testes).
     */
    public void removeOnlinePlayer(UUID uuid) {
        String name = onlinePlayers.remove(uuid);
        if (name != null) {
            playerNameToUUID.remove(name.toLowerCase());
        }
    }
}
