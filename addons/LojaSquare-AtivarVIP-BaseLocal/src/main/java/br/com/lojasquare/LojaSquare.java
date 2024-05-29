package br.com.lojasquare;

import br.com.lojasquare.core.CheckService;
import br.com.lojasquare.core.autoconfig.CheckCreateGroupItem;
import br.com.lojasquare.core.delivery.CheckDelivery;
import br.com.lojasquare.listener.ProdutoListener;
import br.com.lojasquare.providers.database.IDbProvider;
import br.com.lojasquare.providers.database.impl.IDbProviderImpl;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.providers.lojasquare.impl.LSProviderImpl;
import br.com.lojasquare.utils.ConfigManager;
import br.com.lojasquare.utils.DB;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static br.com.lojasquare.utils.Constants.PREFIXO;
import static br.com.lojasquare.utils.Constants.PREFIXO_ERRO;

public class LojaSquare extends JavaPlugin{
	private static LojaSquare pl;
	private static int tempoChecarItens;
	private static List<String> produtosAtivados = new ArrayList<>();
	private static List<String> produtosConfigurados = new ArrayList<>();
	@Getter @Setter
	private static DB db;
	private static String servidor;
	private static boolean debug,smartDelivery;
	private static ConfigManager confGrupos;
	//
	@Getter @Setter private ILSProvider lsProvider;
	@Getter @Setter private IDbProvider iDbProvider;
	
	public void onEnable() {
		ConsoleCommandSender b = Bukkit.getConsoleSender();
		try{
			defineVariaveisAmbiente();
			b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
			if(!checarServidorConfigurado(b)) return;
			
			b.sendMessage(PREFIXO+" §bAtivado...");
			b.sendMessage("§3Criador: §bTrow");
			b.sendMessage("§bDesejo a voce uma otima experiencia com a §dLojaSquare§b.");
			// Carregando todos os grupos de produtos configurados
			carregaGruposEntregaConfigurados(b);
			setDb(DB.load(getMsg("SQL.Host"), getMsg("SQL.Database"), getMsg("SQL.User"), getMsg("SQL.Pass")));
			iDbProvider = new IDbProviderImpl(getDb());
			lsProvider = new LSProviderImpl(iDbProvider, this);

			registraEventosCmds();
			
			checagensDeInicializacao(b);
			
			b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		}catch (Exception e){
			e.printStackTrace();
			b.sendMessage(PREFIXO_ERRO+" §cErro ao iniciar o plugin LojaSquare.");
			b.sendMessage(PREFIXO_ERRO+" §cErro: §a"+e.getMessage());
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	private void registraEventosCmds() {
		Bukkit.getPluginManager().registerEvents(new ProdutoListener(pl,lsProvider), this);
	}

	private void checagensDeInicializacao(ConsoleCommandSender b) {
		List<CheckService> checkServices = new ArrayList<>();
		checkServices.add(new CheckCreateGroupItem(pl, lsProvider));
		checkServices.add(new CheckDelivery(pl, lsProvider));

		checkServices.forEach(s -> s.execute(b));
	}

	private void carregaGruposEntregaConfigurados(ConsoleCommandSender b) {
		confGrupos 	= new ConfigManager("produtos", pl);
		if(confGrupos == null || confGrupos.getString("Grupos") == null) return;
		b.sendMessage(PREFIXO+" §bIniciando o carregamento dos nomes dos grupos de itens para serem entregues...");
		for(String v : confGrupos.getConfigurationSection("Grupos").getKeys(false)){
			produtosConfigurados.add(v);
			if(!confGrupos.getBoolean("Grupos."+v+".Ativado")) {
				b.sendMessage("§4[LojaSquare] §cO grupo §a"+v+"§c nao esta ativado nas configuracoes.");
				continue;
			}
			produtosAtivados.add(v);
			b.sendMessage(PREFIXO+" §bGrupo carregado: §a"+v);
		}
		b.sendMessage(PREFIXO+" §bGrupos de entregas foram carregados com sucesso!");
	}

	private void defineVariaveisAmbiente() {
		pl=this;
		saveDefaultConfig();
		debug 				= getConfig().getBoolean("LojaSquare.Debug",true);
		servidor 			= getConfig().getString("LojaSquare.Servidor",null);
		smartDelivery 		= getConfig().getBoolean("LojaSquare.Smart_Delivery",true);
		// Definindo outras variaveis para nao ir buscar na Config.yml toda vez que for necessario.
		tempoChecarItens 	= getConfig().getInt("Config.Tempo_Checar_Compras",60);
	}

	public void onDisable() {
		ConsoleCommandSender b = Bukkit.getConsoleSender();
		b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		b.sendMessage(PREFIXO+" §bDesativado...");
		b.sendMessage("§3Criador: §bTrow");
		b.sendMessage("§bAgradeco por usar meu(s) plugin(s)");
		b.sendMessage("§6=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
	}
	
	public boolean checarServidorConfigurado(ConsoleCommandSender b){
		if(servidor==null||servidor.equalsIgnoreCase("Nome-Do-Servidor")){
			b.sendMessage(PREFIXO_ERRO+" §cDesativando...");
			b.sendMessage(PREFIXO_ERRO+" §cPara que o plugin seja ativado com sucesso, e necessario configurar o nome do seu servidor na config.yml");
			b.sendMessage(PREFIXO_ERRO+" §cAtualmente o nome do servidor esta definido como: §a"+(servidor==null?"§4NAO DEFINIDO":servidor));
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
	
	public int getTempoChecarItens(){
		if(tempoChecarItens<5) return 5;
		return tempoChecarItens;
	}
	
	public boolean doSmartDelivery(){
		return smartDelivery;
	}

	public boolean produtoAtivado(String grupo){
		return produtosAtivados.contains(grupo);
	}

	public static List<String> getProdutosConfigurados() {
		return produtosConfigurados;
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

	public static ConfigManager getConfGrupos() {
		return confGrupos;
	}
	
}
