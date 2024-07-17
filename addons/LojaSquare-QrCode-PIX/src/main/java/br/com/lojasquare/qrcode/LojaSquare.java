package br.com.lojasquare.qrcode;

import br.com.lojasquare.qrcode.core.CheckService;
import br.com.lojasquare.qrcode.core.autoconfig.CheckCreateGroupItem;
import br.com.lojasquare.qrcode.core.gui.OpenGuiConfirmar;
import br.com.lojasquare.qrcode.core.gui.OpenGuiPrincipal;
import br.com.lojasquare.qrcode.core.message.MessageSchedulerService;
import br.com.lojasquare.qrcode.core.produtos.MapProductOfferInGame;
import br.com.lojasquare.qrcode.listener.CommandListener;
import br.com.lojasquare.qrcode.listener.FecharInventarioListener;
import br.com.lojasquare.qrcode.listener.MenuConfirmarListener;
import br.com.lojasquare.qrcode.listener.MenuPrincipalListener;
import br.com.lojasquare.qrcode.providers.lojasquare.ILSProvider;
import br.com.lojasquare.qrcode.providers.request.IRequestProvider;
import br.com.lojasquare.qrcode.utils.PluginLoadUtil;
import br.com.lojasquare.qrcode.utils.SiteUtil;
import br.com.lojasquare.qrcode.utils.bukkit.ConfigManager;
import br.com.lojasquare.qrcode.utils.bukkit.Item;
import br.com.lojasquare.qrcode.utils.bukkit.idlibrary.IDList;
import br.com.lojasquare.qrcode.utils.bukkit.idlibrary.IDMain;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfoGUI;
import br.com.lojasquare.qrcode.utils.versions.ItemValidation;
import br.com.lojasquare.qrcode.utils.versions.impl.ItemValidationBukkit_v_1_7;
import br.com.lojasquare.qrcode.utils.versions.impl.ItemValidationBukkit_v_1_8;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Setter
@Getter
public class LojaSquare extends JavaPlugin {
	
	private static LojaSquare pl;
	private static SiteUtil ls;
	private static String servidor;
	private static boolean debug;
	@Getter
    private static ConfigManager confGrupos;
	//
	private ItemValidation itemValidation;
	private boolean bukkitVersionAcima18;
	private List<ProdutoInfoGUI> listaProdutos;
	private List<String> produtosConfigurados;
	private List<String> mensagensAnuncio;
	private IDMain idMain;
	private ILSProvider lsProvider;
	private IRequestProvider requestProvider;
	private OpenGuiPrincipal openGuiPrincipal;
	private OpenGuiConfirmar openGuiConfirmar;
	
	public void onEnable() {
		ConsoleCommandSender b = Bukkit.getConsoleSender();
		try{
			PluginLoadUtil plu = new PluginLoadUtil();
			defineVariaveisAmbiente();
			if(!Item.defineVersion()) {
				return;
			}
			String keyapi 	= getKeyAPI();
			b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
			if(!checarServidorConfigurado(b)) return;
			validaVersaoBukkit(getServer().getPluginManager(), b);
			
			b.sendMessage("§3[LSQrCode] §bAtivado...");
			b.sendMessage("§3Criador: §bTrow");
			b.sendMessage("§bDesejo a voce uma otima experiencia com a §dLojaSquare§b.");

			// Carregando todos os grupos de produtos configurados
			carregaGruposEntregaConfigurados(b);
			
			// INICIO definindo variaveis do LojaSquare
			plu.prepareWebServiceConnection(b, keyapi, getPublicAPI(), lsProvider, requestProvider, ls, pl);
			idMain = new IDMain(new IDList());
			// FIM definindo variaveis do LojaSquare

			registraEventosCmds();
			
			checagensDeInicializacao(b);
			
			b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		}catch (Exception e){
			e.printStackTrace();
			b.sendMessage("§4[LSQrCode] §cErro ao iniciar o plugin LSQrCode.");
			b.sendMessage("§4[LSQrCode] §cErro: §a"+e.getMessage());
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	private void registraEventosCmds() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new MenuPrincipalListener(pl), this);
		pm.registerEvents(new MenuConfirmarListener(pl), this);
		pm.registerEvents(new CommandListener(pl), this);
		if(getConfig().getBoolean("Config.Fechar_Inv_Checar_Se_Tem_Item_Indevido")) {
			pm.registerEvents(new FecharInventarioListener(pl), this);
		}
	}

	private void validaVersaoBukkit(PluginManager pm, ConsoleCommandSender b) {
		if(Item.isAboveBukkit18()) {
			setItemValidation(new ItemValidationBukkit_v_1_8(this));
			setBukkitVersionAcima18(true);
			if (pm.getPlugin("NBTAPI") == null && pm.getPlugin("AtlasLicense") == null) {
				b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
				b.sendMessage("§3[LSQrCode] §bDesativado...");
				b.sendMessage("§3Criador: §bTrow");
				b.sendMessage("§bAgradeco por usar meu(s) plugin(s)");
				b.sendMessage("§4Motivo: §cNBTAPI nao encontrado! Baixe em: https://www.spigotmc.org/resources/nbt-api.7939/");
				b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
				pm.disablePlugin(this);
				return;
			}
		} else {
			setItemValidation(new ItemValidationBukkit_v_1_7(this));
		}
	}

	private void checagensDeInicializacao(ConsoleCommandSender b) {
		List<CheckService> checkServices = new ArrayList<>();
		checkServices.add(new CheckCreateGroupItem(pl, lsProvider));
		checkServices.add(new MapProductOfferInGame(pl));
		checkServices.add(new MessageSchedulerService(pl));

		checkServices.forEach(s -> s.execute(b));
	}

	private void carregaGruposEntregaConfigurados(ConsoleCommandSender b) {
		confGrupos = new ConfigManager("produtos", pl);
		if(confGrupos == null || confGrupos.getString("Grupos") == null) return;
		b.sendMessage("§3[LSQrCode] §bIniciando o carregamento dos nomes dos grupos de itens para serem entregues...");
		for(String v : confGrupos.getConfigurationSection("Grupos").getKeys(false)){
			produtosConfigurados.add(v);
			b.sendMessage("§3[LSQrCode] §bGrupo carregado: §a"+v);
		}
		b.sendMessage("§3[LSQrCode] §bGrupos de entregas foram carregados com sucesso!");
	}

	private void defineVariaveisAmbiente() {
		setListaProdutos(new ArrayList<>());
		setProdutosConfigurados(new ArrayList<>());
		setMensagensAnuncio(new ArrayList<>());
		pl=this;
		saveDefaultConfig();
		debug 				= getConfig().getBoolean("LojaSquare.Debug",true);
		servidor 			= getConfig().getString("LojaSquare.Servidor",null);
		setOpenGuiPrincipal(new OpenGuiPrincipal(this));
		setOpenGuiConfirmar(new OpenGuiConfirmar(this, new HashMap<>()));
		for(String s : pl.getConfig().getStringList("Config.Anuncio.Mensagem")) {
			getMensagensAnuncio().add(s.replace("&", "§"));
		}
	}

	public void onDisable() {
		ConsoleCommandSender b = Bukkit.getConsoleSender();
		b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		b.sendMessage("§3[LSQrCode] §bDesativado...");
		b.sendMessage("§3Criador: §bTrow");
		b.sendMessage("§bAgradeco por usar meu(s) plugin(s)");
		b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
	}
	
	public boolean checarServidorConfigurado(ConsoleCommandSender b){
		if(servidor==null||servidor.equalsIgnoreCase("Nome-Do-Servidor")){
			b.sendMessage("§4[LSQrCode] §cDesativando...");
			b.sendMessage("§4[LSQrCode] §cPara que o plugin seja ativado com sucesso, e necessario configurar o nome do seu servidor na config.yml");
			b.sendMessage("§4[LSQrCode] §cAtualmente o nome do servidor esta definido como: §a"+(servidor==null?"§4NAO DEFINIDO":servidor));
			b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
			Bukkit.getPluginManager().disablePlugin(getInstance());
			return false;
		}
		return true;
	}
	
	public boolean canDebug(){
		return debug;
	}
	
	public static void printDebug(String s){
		if(debug){
			Bukkit.getConsoleSender().sendMessage(s);
			for(Player p:getOnlinePlayers()){
				if(p.isOp()||p.hasPermission("lojasquare.debug")){
					p.sendMessage(s);
				}
			}
		}
	}
	
	public static Player[] getOnlinePlayers() {
		try {
			Method method = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
			Object players = method.invoke(null);
			if (players instanceof Player[]) {
				return (Player[]) players;
			} else {
				Collection<?> c = ((Collection<?>) players);
				return c.toArray(new Player[c.size()]);
			}

		} catch (Exception e) {
		}
		return null;
	}
	
	public void setSiteUtil(SiteUtil su) {
		ls = su;
	}
	
	public SiteUtil getLojaSquare(){
		return ls;
	}

	public String getKeyAPI(){
		return getMsg("LojaSquare.SECRET_KEY");
	}

	public String getPublicAPI(){
		return getMsg("LojaSquare.KEY_API");
	}

	public String getMsg(String s){
		try {
			return getConfig().getString(s).replace("&", "§");
		} catch (Exception e) {
			Bukkit.broadcastMessage("§cLinha nao encontrada na config: §a"+s);
			return "";
		}
	}
	
	public void print(String s){
		Bukkit.getConsoleSender().sendMessage(s);
	}
	
	public static LojaSquare getInstance(){
		return pl;
	}

	public String getServidor() {
		return servidor;
	}

	private void setServidor(String servidor) {
		this.servidor = servidor;
	}

    public static void setConfGrupos(ConfigManager confGrupos) {
		confGrupos = confGrupos;
	}
	
}
