# Plano de Migração: Spigot → Fabric (MC 1.21.1)

## Objetivo

Portar o plugin LojaSquare v2.0 de Spigot/Bukkit (MC 1.16.4, Java 8) para **Fabric** (MC 1.21.1, Java 21), preservando toda lógica de negócio, mantendo camadas providers/utils inalteradas.

> **Nota**: Este plano cobre Fabric como alvo primário. Para referência, Forge 52.1.x é a versão compatível com MC 1.21.1 (ver seção 11).

---

## 1. Estrutura do Projeto Fabric

```
fabric/
├── build.gradle                    # fabric-loom-remap 1.6.11
├── gradle.properties               # MC 1.21.1, Loader 0.16.9, Fabric API 0.115.0+1.21.1
├── settings.gradle                 # rootProject.name = "lojasquare"
├── gradlew / gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties  # Gradle 8.10
│
└── src/
    └── main/
        ├── java/br/com/lojasquare/
        │   ├── LojasquareMod.java                 # ModInitializer
        │   ├── api/
        │   │   ├── ProductPreActiveCallback.java  # Fabric Event
        │   │   └── ProductActiveCallback.java     # Fabric Event
        │   ├── command/
        │   │   └── LojasquareCommands.java        # Brigadier commands
        │   ├── core/
        │   │   ├── CheckService.java              # ⇐ IDÊNTICO
        │   │   ├── autoconfig/
        │   │   │   └── CheckCreateGroupItem.java  # ⇐ IDÊNTICO (só ConfigManager)
        │   │   └── delivery/
        │   │       └── CheckDelivery.java         # ServerTickEvents
        │   ├── listener/
        │   │   └── DeliveryListener.java          # Event receiver
        │   ├── providers/
        │   │   ├── lojasquare/
        │   │   │   ├── ILSProvider.java           # ⇐ IDÊNTICO
        │   │   │   └── impl/
        │   │   │       └── LSProviderImpl.java    # ⇐ IDÊNTICO
        │   │   └── request/
        │   │       ├── IRequestProvider.java      # ⇐ IDÊNTICO
        │   │       └── impl/
        │   │           └── RequestProviderImpl.java # ⇐ IDÊNTICO
        │   └── utils/
        │       ├── ConfigManager.java             # REWRITE: SnakeYAML
        │       ├── DateDuration.java              # ⇐ IDÊNTICO
        │       ├── HttpResponse.java              # ⇐ IDÊNTICO
        │       ├── PluginLoadUtil.java            # Ajuste construtor
        │       ├── SiteUtil.java                  # ⇐ IDÊNTICO
        │       ├── TestCall.java                  # ⇐ IDÊNTICO
        │       ├── enums/
        │       │   ├── LSEntregaStatus.java       # ⇐ IDÊNTICO
        │       │   └── LSResponseEnum.java        # ⇐ IDÊNTICO
        │       └── model/
        │           ├── ItemInfo.java              # ⇐ IDÊNTICO
        │           ├── ProdutoInfo.java           # ⇐ IDÊNTICO
        │           └── ValidaIpInfo.java          # ⇐ IDÊNTICO
        │
        └── resources/
            ├── fabric.mod.json                    # Metadados Fabric
            ├── assets/lojasquare/icon.png
            ├── config.yml                         # Config padrão
            └── produtos.yml                       # Produtos padrão
```

---

## 2. O que NÃO muda (preservar idêntico)

| Pacote | Arquivos | Razão |
|--------|----------|-------|
| `providers/lojasquare/` | `ILSProvider.java`, `LSProviderImpl.java` | HTTP puro (HttpsURLConnection), zero dependência Bukkit |
| `providers/request/` | `IRequestProvider.java`, `RequestProviderImpl.java` | HTTP puro |
| `utils/model/` | `ItemInfo.java`, `ProdutoInfo.java`, `ValidaIpInfo.java` | POJOs Lombok, sem acoplamento |
| `utils/enums/` | `LSEntregaStatus.java`, `LSResponseEnum.java` | Enums puros |
| `utils/` | `DateDuration.java`, `HttpResponse.java`, `SiteUtil.java`, `TestCall.java` | POJOs/utilitários puros |
| `core/` | `CheckService.java` | Interface pura (`void execute(...)`) |

### Ajustes mínimos em:

| Arquivo | Mudança |
|---------|---------|
| `PluginLoadUtil.java` | Trocado `ConsoleCommandSender` por `MinecraftServer` |
| `CheckCreateGroupItem.java` | `ConsoleCommandSender` → `MinecraftServer` + `LOGGER.info()` |
| `CheckDelivery.java` | `ConsoleCommandSender` → `MinecraftServer` + `LOGGER.info()` |

---

## 3. Mapeamento Spigot → Fabric (Yarn mappings)

> **Atenção**: MC 1.21.1 usa **Yarn mappings** (não Mojang). Nomes de classe abaixo seguem Yarn.
> Principais diferenças: `PlayerEntity` (não `Player`), `ServerCommandSource` (não `CommandSourceStack`),
> `Text` (não `Component`), `ActionResult` (não `InteractionResult`).

### 3.1 LojaSquare.java → LojasquareMod.java

| Spigot | Fabric (Yarn 1.21.1) |
|--------|----------------------|
| `extends JavaPlugin` | `implements ModInitializer` |
| `onEnable()` | `onInitialize()` + `ServerLifecycleEvents.SERVER_STARTED` |
| `onDisable()` | `ServerLifecycleEvents.SERVER_STOPPING` |
| `ConsoleCommandSender b = Bukkit.getConsoleSender()` | `LojasquareMod.LOGGER.info("mensagem")` (SLF4J) |
| `getConfig().getString(...)` | `ConfigManager.getString(path)` (SnakeYAML) |
| `saveDefaultConfig()` | Copiar resource → `config/lojasquare/` em onInitialize |
| `Bukkit.getPluginManager().registerEvents()` | `ProductPreActiveCallback.EVENT.register(this::handler)` |
| `getCommand("lsite").setExecutor()` | `CommandRegistrationCallback.EVENT.register(...)` |
| `BukkitRunnable` asíncrono | `CompletableFuture.runAsync(runnable, executor)` |
| `BukkitRunnable` síncrono | `server.execute(runnable)` |
| `getOnlinePlayers()` reflection | `server.getPlayerManager().getPlayerList()` |
| `Bukkit.getPlayer(name)` | `server.getPlayerManager().getPlayer(name)` |
| `Bukkit.dispatchCommand(console, cmd)` | `server.getCommandManager().executeWithPrefix(source, cmd)` |
| `player.sendMessage(msg)` | `player.sendMessage(Text.literal(msg))` |
| Mensagens com `&` color | `msg.replace("&", "§")` + `Text.literal(...)` |
| `printDebug()` | `if (debug) { LOGGER.info(msg); enviar para OP/perm }` |
| `print()` | `LOGGER.info(msg)` |
| Singleton `getInstance()` | Manter mesmo padrão `private static LojasquareMod instance` |
| `getMsg(path)` | Ler via ConfigManager `config.getString(path)` |
| `produtosAtivados/produtosConfigurados` | Manter como `static List<String>` igual |
| `tempoChecarItens`, `servidor`, `debug`, `smartDelivery` | Manter como `static` igual |

#### Fluxo `onInitialize()`:

```
1. instance = this
2. Criar diretório config/lojasquare/
3. Copiar config.yml e produtos.yml default se não existirem
4. Carregar ConfigManager (config.yml)
5. definiVariaveisAmbiente() - ler debug, servidor, smartDelivery, tempo
6. Validar servidor configurado (se não, log + return)
7. Carregar grupos do produtos.yml
8. Criar SiteUtil, RequestProviderImpl, LSProviderImpl (via PluginLoadUtil)
9. Registrar comandos (CommandRegistrationCallback)
10. Registrar eventos customizados (callbacks)
11. Registrar ServerLifecycleEvents.SERVER_STARTED:
    a. Servidor iniciado completamente → checarIPCorreto + checagensInicializacao
```

### 3.2 CmdMain.java → LojasquareCommands.java

| Spigot | Fabric (Yarn 1.21.1) |
|--------|----------------------|
| `implements CommandExecutor` | Classe com static register + Brigadier |
| `onCommand(CommandSender, Command, String, String[])` | `dispatcher.register(Commands.literal("lsite")...)` |
| `sender instanceof Player` check | `ctx.getSource().getPlayer()` retorna null se não for player |
| `sender.sendMessage(msg)` | `source.sendFeedback(() -> Text.literal(msg), false)` |
| `runTaskAsynchronously(pl)` | `CompletableFuture.runAsync(...)` no executor custom |
| Acesso a `sender.getName()` | `ctx.getSource().getName()` |

#### Estrutura do comando `/lsite`:

```
/lsite ativar <codigo>
  → valida se source.getPlayer() != null
  → async: lsProvider.activateAccount(codigo, playerName)
  → sync callback: sendFeedback ou sendError
```

### 3.3 Eventos Customizados (Bukkit → Fabric)

**ProdutoPreActiveEvent / ProductActiveEvent** viram interfaces callback Fabric.

Modelo:

```java
// ProductPreActiveCallback.java
public interface ProductPreActiveCallback {
    Event<ProductPreActiveCallback> EVENT = EventFactory.createArrayBacked(
        ProductPreActiveCallback.class,
        (listeners) -> (player, item) -> {
            for (ProductPreActiveCallback listener : listeners) {
                ActionResult result = listener.onPreActive(player, item);
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            return ActionResult.PASS;
        }
    );
    ActionResult onPreActive(PlayerEntity player, ItemInfo item);
}

// ProductActiveCallback.java
public interface ProductActiveCallback {
    Event<ProductActiveCallback> EVENT = EventFactory.createArrayBacked(
        ProductActiveCallback.class,
        (listeners) -> (player, item) -> {
            for (ProductActiveCallback listener : listeners) {
                ActionResult result = listener.onActive(player, item);
                if (result != ActionResult.PASS) {
                    return result;
                }
            }
            return ActionResult.PASS;
        }
    );
    ActionResult onActive(PlayerEntity player, ItemInfo item);
}
```

**Importante**: Yarn `ActionResult` (não `InteractionResult`).

| Ação Spigot | Equivalente Fabric |
|---|---|
| `new ProductPreActiveEvent(player, item)` | Direct call: callback carrega player+item no lambda |
| `Bukkit.getPluginManager().callEvent(event)` | `ProductPreActiveCallback.EVENT.invoker().onPreActive(player, item)` |
| `event.isCancelled()` | `ActionResult result != ActionResult.PASS` |
| `event.setCancelled(true)` | `return ActionResult.FAIL` |
| `@EventHandler` | `ProductPreActiveCallback.EVENT.register(this::handler)` |

### 3.4 ProdutoListener.java → DeliveryListener.java

| Spigot | Fabric (Yarn 1.21.1) |
|--------|----------------------|
| `implements Listener` | Classe pura sem interface |
| `@EventHandler preActive(ProductPreActiveEvent)` | `ProductPreActiveCallback.EVENT.register(this::preActive)` |
| `@EventHandler activeDelivery(ProductActiveEvent)` | `ProductActiveCallback.EVENT.register(this::activeDelivery)` |
| `BukkitRunnable` async p/ updateDelivery | `CompletableFuture.supplyAsync(() -> lsProvider.updateDelivery(ii))` |
| `new ProductActiveEvent(...)` | Chamar invoker direto: `ProductActiveCallback.EVENT.invoker().onActive(player, ii)` |
| `runTask(pl)` p/ sync | `server.execute(() -> ProductActiveCallback.EVENT.invoker()...)` |
| `Bukkit.dispatchCommand(console, cmd)` | `server.getCommandManager().executeWithPrefix(server.getCommandSource(), cmd)` |
| `player.sendMessage(s.replace("&", "§"))` | `player.sendMessage(Text.literal(s.replace("&", "§")))` |

#### replaceString():
Método permanece IGUAL — só manipula `String.replace()`, sem dependência de API.

### 3.5 CheckDelivery.java

| Spigot | Fabric (Yarn 1.21.1) |
|--------|----------------------|
| `new BukkitRunnable() { run() { ... } }.runTaskTimerAsynchronously(pl, 20*10, 20*X)` | `ServerTickEvents.END_SERVER_TICK.register(server -> { tickCount++; if (tickCount % (20 * X) == 0) { ... } })` |
| `List<ItemInfo> itens = lsProvider.getTodasEntregas(PENDENTE)` | Executar em `CompletableFuture.supplyAsync()` — HTTP blocking |
| Loop síncrono sobre itens | Callback do future processa na server thread |
| `Bukkit.getPlayer(item.getPlayer())` | `server.getPlayerManager().getPlayer(item.getPlayer())` |
| `p.getInventory().getContents()` | `p.getInventory().main` (Yarn: `main`, não `items`) |
| `p.getInventory().getArmorContents()` | `p.getInventory().armor` (mesmo em Yarn) |
| `Material.AIR` | `Items.AIR` (net.minecraft.item.Items) |
| `item.getType() != Material.AIR` | `!itemStack.isEmpty()` (método nativo em Yarn) |
| `callEvent(new ProductPreActiveEvent(p, item))` | `ProductPreActiveCallback.EVENT.invoker().onPreActive(p, item)` |

### 3.6 ConfigManager.java (YamlConfiguration → SnakeYAML)

| Spigot (YamlConfiguration do Bukkit) | Fabric (SnakeYAML) |
|---|---|
| `extends YamlConfiguration` | Wrapper em `org.yaml.snakeyaml.Yaml` |
| `new ConfigManager("produtos", plugin)` | `new ConfigManager(configDir.resolve("produtos.yml"))` |
| `getString("Grupos.VIP.Ativado")` | `(String) getNested("Grupos.VIP.Ativado")` |
| `getBoolean("Grupos.VIP.Money")` | `(Boolean) getNested(...)` ou `Boolean.parseBoolean()` |
| `getInt("...")` | `((Number) getNested(...)).intValue()` |
| `getDouble("...")` | `((Number) getNested(...)).doubleValue()` |
| `getStringList("...")` | `(List<String>) getNested(...)` |
| `getConfigurationSection("Grupos").getKeys(false)` | `((Map<String, Object>) data.get("Grupos")).keySet()` |
| `set(path, obj) + auto-save` | `setNested(path, obj) + save()` |
| `save()` | `yaml.dump(data, new FileWriter(path))` |
| `reload()` | `yaml.load(new FileReader(path))` |

Usar `org.yaml.snakeyaml.Yaml` (declarado no build.gradle como dependência explícita).

---

## 4. Config Files

### Localização

```
.minecraft/
└── config/
    └── lojasquare/
        ├── config.yml         # SECRET_API, Servidor, Token, Timeouts
        └── produtos.yml       # Grupos, comandos, mensagens
```

Em vez de `plugins/LojaSquare/config.yml` (Spigot), usar `config/lojasquare/config.yml` (padrão Fabric).

### config.yml

```yaml
LojaSquare:
  SECRET_API: "sua-chave-aqui"
  Servidor: "Nome-Do-Servidor"
  Token_Servidor: "token-do-servidor"
  Connection_Timeout: 10000
  Read_Timeout: 10000
  Smart_Delivery: true
  Debug: true
Config:
  Tempo_Checar_Compras: 60
```

### produtos.yml

```yaml
Grupos:
  VIPDiamante:
    Ativado: true
    Ativar_Com_Player_Offline: false
    Enviar_Mensagem: true
    Mensagem_Receber_Ao_Ativar_Produto:
      - "&eOla &a@player"
      - "&eSeu &bVIP Diamante &efoi ativado!"
    Money: false
    Quantidade_De_Money: 0
    Smart_Delivery: true
    Cmds_A_Executar:
      - "lp user @player parent set vipdiamante"
```

---

## 5. Build System

### build.gradle

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.fabricmc.fabric-loom-remap' version '1.6.11'
}

base {
    archivesName = project.archives_base_name
}

version = project.mod_version
group = project.maven_group

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"

    compileOnly "org.projectlombok:lombok:1.18.32"
    annotationProcessor "org.projectlombok:lombok:1.18.32"

    implementation "org.yaml:snakeyaml:2.3"
}

processResources {
    inputs.property "version", project.version
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = 21
}
```

### gradle.properties

```properties
org.gradle.jvmargs=-Xmx1G
org.gradle.parallel=true

minecraft_version=1.21.1
yarn_mappings=1.21.1+build.3
loader_version=0.16.9
loom_version=1.6.11
mod_version=2.0.0
maven_group=br.com.lojasquare
archives_base_name=lojasquare
fabric_api_version=0.115.0+1.21.1
```

### settings.gradle

```groovy
pluginManagement {
    repositories {
        maven { url "https://maven.fabricmc.net/" }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "lojasquare"
```

### fabric.mod.json

```json
{
    "schemaVersion": 1,
    "id": "lojasquare",
    "version": "${version}",
    "name": "LojaSquare",
    "description": "Integracao do servidor Minecraft com a plataforma LojaSquare para venda e entrega automatizada de produtos.",
    "authors": ["TrowDev"],
    "contact": {
        "homepage": "https://lojasquare.net/",
        "sources": "https://github.com/TrowDev/LS-Plugins"
    },
    "license": "Proprietary",
    "icon": "assets/lojasquare/icon.png",
    "environment": "*",
    "entrypoints": {
        "main": ["br.com.lojasquare.LojasquareMod"]
    },
    "depends": {
        "fabricloader": ">=0.16.0",
        "minecraft": "~1.21.1",
        "java": ">=21",
        "fabric-api": "*"
    }
}
```

---

## 6. APIs Fabric Utilizadas (MC 1.21.1)

| Módulo Fabric API | Uso |
|---|---|
| `fabric-api-base` | `EventFactory.createArrayBacked()` para eventos custom |
| `fabric-lifecycle-events-v1` | `ServerLifecycleEvents.SERVER_STARTED/STOPPING` |
| `fabric-command-api-v2` | `CommandRegistrationCallback` para `/lsite` |
| `fabric-networking-api-v1` | `PlayerLookup.all(server)` (opcional) |
| `fabric-api` (agregado) | Dependency declaration |

---

## 7. Ordem de Implementação

| Fase | Arquivos | Descrição | Depende |
|------|----------|-----------|---------|
| 1 | `build.gradle`, `gradle.properties`, `settings.gradle`, `fabric.mod.json` | Setup build system + metadados | Nada |
| 2 | `gradle/wrapper/gradle-wrapper.properties` | Gradle wrapper | Fase 1 |
| 3 | `utils/ConfigManager.java` | Rewrite SnakeYAML | Nada |
| 4 | `utils/PluginLoadUtil.java` | Remover `ConsoleCommandSender`, usar `MinecraftServer` | Fase 3 |
| 5 | `LojasquareMod.java` | Classe principal, ServerLifecycleEvents | Fase 3, 4 |
| 6 | `api/ProductPreActiveCallback.java`, `ProductActiveCallback.java` | Eventos Fabric | Fase 5 |
| 7 | `command/LojasquareCommands.java` | Comando `/lsite` Brigadier | Fase 5 |
| 8 | `listener/DeliveryListener.java` | Recebe eventos, executa entregas | Fase 5, 6 |
| 9 | `core/delivery/CheckDelivery.java` | Timer tick, validações, disparo eventos | Fase 5, 6, 8 |
| 10 | `core/autoconfig/CheckCreateGroupItem.java` | Auto-configuração produtos | Fase 3, 5 |
| 11 | Resources: `config.yml`, `produtos.yml`, `icon.png` | Arquivos padrão | Fase 5 |

---

## 8. Riscos e Atenção

| Risco | Mitigação |
|-------|-----------|
| `org.yaml.snakeyaml.Yaml` não disponível | Declarar `implementation "org.yaml:snakeyaml:2.3"` no build.gradle |
| Lombok `@AllArgsConstructor` com `ConsoleCommandSender` | Substituir parâmetro por `MinecraftServer` nos construtores |
| Gson pode ter versão conflitante | Usar o bundled do Minecraft ou declarar versão explícita |
| `ServerCommandSource` (Yarn) vs `CommandSourceStack` (Mojang) | MC 1.21.1 usa Yarn → `net.minecraft.server.command.ServerCommandSource` |
| `dispatchCommand()` no Spigot aceita string, Fabric precisa de `ServerCommandSource` | Usar `server.getCommandManager().executeWithPrefix(server.getCommandSource(), cmd)` |
| Async HTTP blocking (`updateDelivery`) não pode rodar na server thread | `CompletableFuture.supplyAsync()` com executor thread pool |
| `player.getInventory().getContents()` → Yarn `main` | `player.getInventory().main` em vez de `items` |
| `Material.AIR` não existe em Fabric | `itemStack.isEmpty()` é o padrão; ou `Items.AIR` para comparação |
| `Text` vs `Component` | 1.21.1 usa `net.minecraft.text.Text` (não `Component`) |
| `ActionResult` vs `InteractionResult` | 1.21.1 usa `net.minecraft.util.ActionResult` |
| `Identifier` construtor | Yarn: `new Identifier("lojasquare", "path")`, não `Identifier.fromNamespaceAndPath()` |

---

## 9. Checklist de Portabilidade

- [ ] Fase 1: Setup Gradle + fabric.mod.json
- [ ] Fase 2: ConfigManager com SnakeYAML
- [ ] Fase 3: LojasquareMod com lifecycle
- [ ] Fase 4: Eventos custom Fabric
- [ ] Fase 5: Comando `/lsite`
- [ ] Fase 6: DeliveryListener
- [ ] Fase 7: CheckDelivery com tick timer
- [ ] Fase 8: CheckCreateGroupItem
- [ ] Fase 9: Resources padrão

---

## 10. Referências

- Fabric docs: https://docs.fabricmc.net/develop/events
- Command basics: https://docs.fabricmc.net/develop/commands/basics
- Fabric Loom 1.6: https://docs.fabricmc.net/develop/loom/
- ServerLifecycleEvents: fabric-api-base
- EventFactory: fabric-api-base
- Fabric Developer Versions: https://fabricmc.net/develop/
- Yarn mappings: https://github.com/FabricMC/yarn
- código fonte spigot: `../spigot/src/main/java/br/com/lojasquare/`
- Snapshot original: `../spigot/README.md`

---

## 11. Forge 1.21.1 (Referência)

Se houver necessidade de portar para Forge em vez de Fabric:

| Item | Versão |
|------|--------|
| Minecraft | 1.21.1 |
| **Forge (Latest)** | **52.1.15** |
| **Forge (Recommended)** | **52.1.0** |
| Java | 21 |
| Gradle | 8.6+ |
| Plugin | `net.minecraftforge.gradle:ForgeGradle:6.+` |
| Mappings | Official Mojang + Parchment (opcional) |

### Diferenças Fabric → Forge (pontos principais)

| Conceito | Fabric | Forge |
|----------|--------|-------|
| Entry point | `ModInitializer` | `@Mod("modid")` |
| Eventos | `EventFactory.createArrayBacked()` | `@SubscribeEvent` + `MinecraftForge.EVENT_BUS` |
| Comandos | `CommandRegistrationCallback` + Brigadier | `RegisterCommandsEvent` + Brigadier |
| Config | Manual (SnakeYAML) | `ModLoadingContext.registerConfig()` |
| Mixins | Fabric Mixin (SpongePowered) | Mixins (ou Forge patches) |
| Scheduler | `ServerTickEvents` | `TickEvent.ServerTickEvent` |

### build.gradle (Forge - referência rápida)

```groovy
plugins {
    id 'net.minecraftforge.gradle' version '[6.0,6.2)'
}

minecraft {
    mappings channel: 'official', version: '1.21.1'
}

dependencies {
    minecraft "net.minecraftforge:forge:1.21.1-52.1.0"
}
```
