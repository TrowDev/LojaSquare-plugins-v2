# LojaSquare Plugin - Documentação Técnica Completa

## 📋 Índice
1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [Finalidade do Projeto](#finalidade-do-projeto)
3. [Arquitetura e Estrutura](#arquitetura-e-estrutura)
4. [API Base - Bukkit/Spigot](#api-base---bukkitspigot)
5. [Chamadas Externas à API LojaSquare](#chamadas-externas-à-api-lojasquare)
6. [Lógicas de Negócio](#lógicas-de-negócio)
7. [Sistema de Listeners](#sistema-de-listeners)
8. [Ativação e Entrega de Produtos](#ativação-e-entrega-de-produtos)
9. [Design Patterns Utilizados](#design-patterns-utilizados)
10. [Auto-Configuração](#auto-configuração)
11. [Sistema de Delivery](#sistema-de-delivery)
12. [Utilitários (Utils)](#utilitários-utils)
13. [Modelos de Dados (DTOs)](#modelos-de-dados-dtos)
14. [Mapeamento Completo de Métodos](#mapeamento-completo-de-métodos)
15. [Guia de Portabilidade](#guia-de-portabilidade)

---

## 🎯 Visão Geral do Projeto

**LojaSquare Plugin v2.0** é um plugin Minecraft (Spigot/Bukkit) que integra servidores de Minecraft com a plataforma LojaSquare, permitindo a venda automatizada e entrega de produtos virtuais (VIPs, cash, itens) diretamente no jogo.

### Tecnologias Utilizadas
- **Java 11**
- **API Base**: Spigot-API 1.16.4
- **Build Tool**: Maven
- **Bibliotecas**: 
  - Lombok (geração de código)
  - Gson (serialização JSON)
  - Bukkit/Spigot API (framework de plugin Minecraft)

---

## 🎪 Finalidade do Projeto

O LojaSquare resolve um problema comum para donos de servidores de Minecraft: **automatizar vendas online e entregas in-game**.

### Problema que Resolve
1. **Venda Online**: Donos de servidor precisam vender VIPs, cash e itens pela web
2. **Custo Elevado**: Desenvolver sites customizados é caro
3. **Entrega Manual**: Entregar produtos manualmente é trabalhoso e sujeito a erros
4. **Múltiplos servidores**: Gestão de diferentes subservidores (Factions, RankUP, etc.)

### Solução Oferecida
- **SaaS (Software as a Service)**: Site responsivo gerenciado pela LojaSquare
- **Assinatura**: Modelo de negócio similar ao Netflix
- **Entrega Automática**: Plugin monitora compras e ativa produtos automaticamente
- **Integração API**: Comunicação segura entre site e servidor via REST API

### Fluxo Geral
```
Cliente compra no site → Pagamento aprovado → API registra entrega → 
Plugin consulta API → Valida regras → Executa comandos → Produto entregue
```

---

## 🏗️ Arquitetura e Estrutura

### Estrutura de Pacotes

```
br.com.lojasquare/
├── LojaSquare.java                    # Classe principal do plugin
├── api/                                # API de eventos customizados
│   ├── ProductActiveEvent.java
│   └── ProductPreActiveEvent.java
├── commands/                           # Comandos do plugin
│   └── CmdMain.java
├── core/                               # Serviços principais do sistema
│   ├── CheckService.java              # Interface para serviços de checagem
│   ├── autoconfig/
│   │   └── CheckCreateGroupItem.java  # Auto-configuração de grupos
│   └── delivery/
│       └── CheckDelivery.java         # Sistema de entrega
├── listener/                           # Listeners de eventos
│   └── ProdutoListener.java
├── providers/                          # Camada de integração
│   ├── lojasquare/                    # Provider da API LojaSquare
│   │   ├── ILSProvider.java
│   │   └── impl/
│   │       └── LSProviderImpl.java
│   └── request/                       # Provider HTTP
│       ├── IRequestProvider.java
│       └── impl/
│           └── RequestProviderImpl.java
└── utils/                             # Utilitários
    ├── ConfigManager.java
    ├── DateDuration.java
    ├── HttpResponse.java
    ├── PluginLoadUtil.java
    ├── SiteUtil.java
    ├── TestCall.java
    ├── enums/
    │   ├── LSEntregaStatus.java
    │   └── LSResponseEnum.java
    └── model/
        ├── ItemInfo.java
        ├── ProdutoInfo.java
        └── ValidaIpInfo.java
```

### Arquitetura em Camadas

```
┌─────────────────────────────────────┐
│   Bukkit/Spigot Framework Layer     │
│   (Eventos, Comandos, Scheduler)    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Application Layer                │
│  (LojaSquare.java - Entry Point)    │
└──────────────┬──────────────────────┘
               │
      ┌────────┴────────┐
      │                 │
┌─────▼──────┐   ┌─────▼──────┐
│  Commands  │   │  Listeners │
│   Layer    │   │   Layer    │
└─────┬──────┘   └─────┬──────┘
      │                 │
      └────────┬────────┘
               │
┌──────────────▼──────────────────────┐
│       Business Logic Layer           │
│   (core/: CheckDelivery,            │
│    CheckCreateGroupItem)            │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│       Provider Layer                 │
│  (ILSProvider, IRequestProvider)    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     External API Layer               │
│  (LojaSquare REST API via HTTPS)    │
└──────────────────────────────────────┘
```

---

## 🔌 API Base - Bukkit/Spigot

### Componentes Bukkit Utilizados

#### 1. **JavaPlugin** (org.bukkit.plugin.java.JavaPlugin)
- **Classe**: `LojaSquare extends JavaPlugin`
- **Finalidade**: Classe base para todos os plugins Bukkit/Spigot
- **Métodos Principais**:
  - `onEnable()`: Executado quando o plugin é ativado
  - `onDisable()`: Executado quando o plugin é desativado
  - `getConfig()`: Acessa o arquivo config.yml
  - `saveDefaultConfig()`: Cria config padrão se não existir
  - `getCommand(String)`: Registra comandos

#### 2. **Bukkit** (org.bukkit.Bukkit)
- **Finalidade**: Classe utilitária estática para acessar servidor
- **Uso no Projeto**:
  - `Bukkit.getConsoleSender()`: Envia mensagens ao console
  - `Bukkit.getPlayer(String)`: Busca jogador online por nome
  - `Bukkit.getPluginManager()`: Gerencia plugins (ativar/desativar/eventos)
  - `Bukkit.dispatchCommand()`: executa comandos do servidor
  - `Bukkit.getOnlinePlayers()`: Lista jogadores online (via reflection)

#### 3. **Player** (org.bukkit.entity.Player)
- **Finalidade**: Representa um jogador conectado
- **Métodos Utilizados**:
  - `player.getName()`: Retorna nome do jogador
  - `player.sendMessage()`: Envia mensagem ao jogador
  - `player.isOp()`: Verifica se é operador
  - `player.hasPermission(String)`: Verifica permissão
  - `player.getInventory()`: Acessa inventário

#### 4. **CommandExecutor** (org.bukkit.command.CommandExecutor)
- **Interface**: Implementada por `CmdMain`
- **Finalidade**: Handler de comandos customizados
- **Método**: `onCommand(CommandSender, Command, String, String[])`

#### 5. **Listener** (org.bukkit.event.Listener)
- **Interface**: Implementada por `ProdutoListener`
- **Finalidade**: Classe que escuta eventos do Bukkit
- **Registro**: `Bukkit.getPluginManager().registerEvents(Listener, Plugin)`

#### 6. **@EventHandler** (org.bukkit.event.EventHandler)
- **Annotation**: Marca métodos que recebem eventos
- **Uso**: 
  ```java
  @EventHandler
  public void onPreActive(ProductPreActiveEvent e) { ... }
  ```

#### 7. **Event System** (org.bukkit.event.Event)
- **Classes Base**:
  - `Event`: Todos eventos estendem esta classe
  - `Cancellable`: Interface para eventos canceláveis
  - `HandlerList`: Gerencia listeners de um evento
- **Custom Events**: `ProductActiveEvent`, `ProductPreActiveEvent`

#### 8. **Scheduler** (org.bukkit.scheduler.BukkitRunnable)
- **Finalidade**: Sistema de tarefas assíncronas e síncronas
- **Métodos Principais**:
  - `runTask(Plugin)`: Executa na thread principal (sync)
  - `runTaskAsynchronously(Plugin)`: Executa em thread separada (async)
  - `runTaskTimerAsynchronously(Plugin, delay, period)`: Executa repetidamente

#### 9. **Inventory API** (org.bukkit.inventory)
- **Classes**:
  - `Inventory`: Representa um inventário
  - `ItemStack`: Representa um item
- **Enums**:
  - `Material.AIR`: Representa espaço vazio

#### 10. **Configuration API** (org.bukkit.configuration)
- **Classes**:
  - `YamlConfiguration`: Leitura/escrita de arquivos YAML
  - `ConfigurationSection`: Seção de configuração
- **Uso**: `ConfigManager extends YamlConfiguration`

### Dependências do Bukkit no POM.xml
```xml
<dependency>
    <groupId>org.spigotmc</groupId>
    <artifactId>spigot-api</artifactId>
    <version>1.16.4-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Plugin Descriptor (plugin.yml)
```yaml
name: LojaSquare
version: 2.0-SNAPSHOT
main: br.com.lojasquare.LojaSquare
author: TrowDev
commands:
  lsite:
    description: Comando principal do LojaSquare
```

---

## 🌐 Chamadas Externas à API LojaSquare

### Endpoint Base
- **URL**: `https://api.lojasquare.net`
- **Protocolo**: HTTPS (TLS/SSL)
- **Autenticação**: Header `Authorization` com SECRET_API

### 1. **GET /v1/entregas/{status}**
- **Método**: `ILSProvider.getTodasEntregas(LSEntregaStatus)`
- **Finalidade**: Buscar todas as entregas pendentes
- **Parâmetros**: `status` (1=PENDENTE, 2=ENTREGUE)
- **Response**: Array JSON de `ItemInfo`
- **Exemplo**:
  ```
  GET /v1/entregas/1?status=1
  ```
- **Response**:
  ```json
  [
    {
      "entregaID": 12345,
      "player": "Steve",
      "produto": "VIP",
      "servidor": "LojaSquare",
      "subServidor": "Factions",
      "grupo": "VIPDiamante",
      "codigo": "ABC123",
      "status": "PENDENTE",
      "statusID": 1,
      "dias": 30,
      "quantidade": 1,
      "cupom": "NATAL2024",
      "atualizadoEm": 1640000000000
    }
  ]
  ```

### 2. **GET /v1/produtos**
- **Método**: `ILSProvider.getTodosProdutosDaLoja()`
- **Finalidade**: Buscar todos os produtos cadastrados na loja
- **Parâmetros**: `tokenSubServidor` (identifica o servidor)
- **Response**: Array JSON de `ProdutoInfo`
- **Exemplo**:
  ```
  GET /v1/produtos?tokenSubServidor=TOKEN_123
  ```
- **Response**:
  ```json
  [
    {"grupo": "VIPDiamante", "produto": "VIP Diamante 30 dias"},
    {"grupo": "Cash", "produto": "100k Cash"}
  ]
  ```

### 3. **PUT /v1/entregas/{id}/entregue**
- **Método**: `ILSProvider.updateDelivery(ItemInfo)`
- **Finalidade**: Marcar entrega como concluída
- **Parâmetros**: `{id}` = entregaID
- **Response**: 200/201/204 para sucesso
- **Exemplo**:
  ```
  PUT /v1/entregas/12345/entregue
  ```

### 4. **PUT /v1/clientes/activate**
- **Método**: `ILSProvider.activateAccount(String, String)`
- **Finalidade**: Ativar conta de cliente no painel
- **Parâmetros**: 
  - `codigo`: Código de ativação
  - `usuario`: Nome do usuário
- **Exemplo**:
  ```
  PUT /v1/clientes/activate?codigo=ABC123&usuario=Steve
  ```

### 5. **GET /v1/sites/extensoes**
- **Método**: `ILSProvider.getIpMaquina()`
- **Finalidade**: Validar IP do servidor
- **Response**: `ValidaIpInfo`
- **Exemplo Response**:
  ```json
  {
    "sucesso": true,
    "ip": "203.0.113.42"
  }
  ```

### Headers HTTP Padrão
```java
Authorization: <SECRET_API>
Accept: application/json
Content-Type: application/json
User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0
```

### Timeouts Configuráveis
- **Connection Timeout**: 10000ms (padrão)
- **Read Timeout**: 10000ms (padrão)
- Configurável via `config.yml`

### Códigos de Resposta HTTP

| Código | Enum | Significado |
|--------|------|-------------|
| 200/201/204 | - | Sucesso |
| 401 | CONEXAO_NAO_AUTORIZADA | Credencial inválida |
| 403 | IP_OU_KEY_BLOQUEADOS | IP ou Key bloqueados |
| 404 | NADA_ENCONTRADO | Sem entregas ou key inválida |
| 405 | ASSINATURA_EXPIROU | Assinatura expirada |
| 406 | NADA_MUDOU | Nenhuma atualização efetuada |
| 409 | IP_NAO_LIBERADO | IP diferente do cadastrado |
| 0 | SEM_CONEXAO | Servidor sem internet |

---

## 🧠 Lógicas de Negócio

### 1. **Validação de IP da Máquina**
**Classe**: `LojaSquare.checarIPCorreto()`

```java
// Fluxo
1. Chama ILSProvider.getIpMaquina()
2. Valida se IP retornado está correto
3. Se incorreto: desativa plugin e exibe mensagem
4. Se correto: armazena IP em SiteUtil
```

**Finalidade**: Segurança - impedir uso não autorizado do plugin

### 2. **Carregamento de Grupos**
**Classe**: `LojaSquare.carregaGruposEntregaConfigurados()`

```java
// Fluxo
1. Lê arquivo produtos.yml
2. Percorre seção "Grupos"
3. Adiciona a produtosConfigurados[]
4. Se Ativado=true: adiciona a produtosAtivados[]
```

**Finalidade**: Mapear quais produtos estão configurados para entrega

### 3. **Auto-criação de Configuração de Grupos**
**Classe**: `CheckCreateGroupItem.execute()`

```java
// Fluxo
1. Busca grupos cadastrados no site via API
2. Compara com grupos configurados localmente
3. Para cada grupo novo:
   a. Cria configuração template em produtos.yml
   b. Marca como "Ativado: false"
   c. Define comandos padrão
```

**Finalidade**: Facilitar configuração inicial de novos produtos

**Template Criado**:
```yaml
Grupos:
  VIPDiamante:
    Ativado: false
    Ativar_Com_Player_Offline: false
    Enviar_Mensagem: false
    Mensagem_Receber_Ao_Ativar_Produto:
      - "&eOla &a@player"
      - "&eO produto que voce adquiriu (&a@produto&e) foi ativado!"
    Money: false
    Quantidade_De_Money: 0
    Cmds_A_Executar:
      - "gerarvip VIPDiamante @dias @qnt @player"
```

### 4. **Sistema de Checagem de Entregas**
**Classe**: `CheckDelivery.execute()`

```java
// Fluxo (executado a cada X segundos)
1. Busca entregas com status PENDENTE via API
2. Para cada entrega:
   a. Valida servidor correto
   b. Valida grupo configurado
   c. Valida player online (se necessário)
   d. Valida inventário vazio (se necessário)
   e. Valida nome do player
   f. Se tudo OK: dispara ProductPreActiveEvent
```

**Regras de Validação**:
- **Servidor**: `item.subServidor == config.Servidor`
- **Grupo**: Grupo deve estar em `produtosAtivados[]`
- **Player Online**: Obrigatório se `Ativar_Com_Player_Offline: false`
- **Inventário Vazio**: Obrigatório se `Entregar_Apenas_Com_Inventario_Vazio: true`
- **Nick Compatível**: `player.getName().equalsIgnoreCase(item.player)`

### 5. **Smart Delivery**
**Classe**: `ProdutoListener.activeDelivery()`

```java
// Smart Delivery = true (padrão)
quantidade = 10
comando = "dar cash @qnt @player"
// Executa: "dar cash 10 Steve" (1x)

// Smart Delivery = false
quantidade = 10
comando = "dar cash @qnt @player"
// Executa: "dar cash 1 Steve" (10x)
```

**Finalidade**: Otimizar performance executando menos comandos

### 6. **Sistema de Placeholders**
**Método**: `ProdutoListener.replaceString()`

| Placeholder | Substitui por | Exemplo |
|-------------|---------------|---------|
| `@player` | Nome do jogador | Steve |
| `@produto` | Nome do produto | VIP Diamante 30d |
| `@grupo` | Grupo configurado | VIPDiamante |
| `@qnt` | Quantidade comprada | 10 |
| `@dias` | Dias do produto | 30 |
| `@money` | Valor monetário (double) | 100.5 |
| `@moneyInteiro` | Valor monetário (int) | 100 |
| `@cupom` | Código do cupom | NATAL2024 |

### 7. **Sistema de Debug**
**Método**: `LojaSquare.printDebug()`

```java
// Ativo quando config.yml: Debug: true
// Envia mensagens para:
- Console
- Players com permissão "lojasquare.debug"
- Players OP
```

**Finalidade**: Diagnosticar problemas de entrega

---

## 👂 Sistema de Listeners

### ProdutoListener

**Implementa**: `org.bukkit.event.Listener`

#### Event 1: ProductPreActiveEvent

```java
@EventHandler
public void preActive(ProductPreActiveEvent e)
```

**Trigger**: Disparado por `CheckDelivery` antes de ativar produto

**Fluxo**:
```
1. Verifica se evento foi cancelado
2. Executa assincronamente (não trava servidor):
   a. Chama updateDelivery() para marcar como entregue na API
   b. Se sucesso:
      - Dispara ProductActiveEvent (thread principal)
   c. Se falha:
      - Loga erro no console
```

**Cancelável**: Sim - outros plugins podem cancelar a entrega

#### Event 2: ProductActiveEvent

```java
@EventHandler
public void activeDelivery(ProductActiveEvent e)
```

**Trigger**: Disparado após confirmação na API

**Fluxo**:
```
1. Verifica se cancelado
2. Lê configurações do grupo:
   - Cmds_A_Executar[]
   - Money (boolean)
   - Quantidade_De_Money
3. Calcula quantidade de money (se aplicável)
4. Executa comandos:
   - Se Smart_Delivery=true: executa 1x com @qnt
   - Se Smart_Delivery=false: executa N vezes
5. Envia mensagem ao player (se configurado)
```

**Cancelável**: Sim - mas produto já foi marcado como entregue na API

#### Método: dispatchCommandDelivery()

```java
private void dispatchCommandDelivery(ItemInfo ii, int qntMoneyInteiro, String cmds)
```

**Finalidade**: Executar comando com tratamento de erro

**Fluxo**:
```
1. Substitui placeholders no comando
2. Executa: Bukkit.dispatchCommand(console, comando)
3. Captura exceções e loga erros
```

#### Método: sendMsgToPlayerOnActiveProducts()

```java
private void sendMsgToPlayerOnActiveProducts(ItemInfo ii, Player p)
```

**Finalidade**: Enviar mensagem customizada ao player

**Condições**:
- Player não pode ser null
- `Enviar_Mensagem: true` no grupo
- Mensagens definidas em `Mensagem_Receber_Ao_Ativar_Produto[]`

---

## 📦 Ativação e Entrega de Produtos

### Fluxo Completo de Entrega

```
┌──────────────────────────────────────────┐
│ 1. Cliente compra produto no site        │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 2. Pagamento aprovado (PayPal/PagSeguro) │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 3. API LojaSquare registra entrega        │
│    Status: PENDENTE                       │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 4. CheckDelivery (timer a cada 60s)      │
│    Consulta: GET /v1/entregas/1          │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 5. Validações (9 regras)                 │
│    ✓ Servidor correto                    │
│    ✓ Grupo configurado e ativado         │
│    ✓ Player online (se necessário)       │
│    ✓ Inventário vazio (se necessário)    │
│    ✓ Nick compatível                     │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 6. Dispara ProductPreActiveEvent         │
│    (Cancelável por outros plugins)       │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 7. ProdutoListener.preActive()           │
│    Chama: PUT /v1/entregas/{id}/entregue │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 8. API atualiza status: ENTREGUE         │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 9. Dispara ProductActiveEvent            │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 10. ProdutoListener.activeDelivery()     │
│     - Lê comandos do produtos.yml        │
│     - Substitui placeholders             │
│     - Executa comandos via console       │
│     - Envia mensagem ao player           │
└──────────────┬───────────────────────────┘
               │
┌──────────────▼───────────────────────────┐
│ 11. Produto entregue ao jogador! ✓       │
└──────────────────────────────────────────┘
```

### Casos Especiais

#### Produtos DISPUTA / RESOLVIDO
- **Grupo**: "DISPUTA" ou "RESOLVIDO"
- **Comportamento**: Permite entrega mesmo com player offline
- **Finalidade**: Resolver problemas via painel administrativo

#### Money Products
```yaml
Grupos:
  Cash:
    Money: true
    Quantidade_De_Money: 100000.0
    Smart_Delivery: true
```

**Cálculo**:
- Smart_Delivery=true: `qntMoney = 100000 * quantidade`
- Smart_Delivery=false: `qntMoney = 100000` (executado N vezes)

---

## 🎨 Design Patterns Utilizados

### 1. **Singleton Pattern**
**Classe**: `LojaSquare`

```java
private static LojaSquare pl;

public static LojaSquare getInstance() {
    return pl;
}
```

**Finalidade**: Garantir uma única instância do plugin

### 2. **Strategy Pattern (Interface-Based)**
**Interfaces**: `ILSProvider`, `IRequestProvider`, `CheckService`

```java
// Permite trocar implementação sem alterar código cliente
ILSProvider provider = new LSProviderImpl(...);
// Futuramente: ILSProvider provider = new MockLSProvider();
```

**Finalidade**: Desacoplamento e testabilidade

### 3. **Dependency Injection (Manual)**
**Exemplo**: `LojaSquare` injeta dependências nos construtores

```java
// LojaSquare injeta dependências
ProdutoListener listener = new ProdutoListener(pl, lsProvider);
CmdMain cmd = new CmdMain(pl, lsProvider);
```

**Finalidade**: Reduzir acoplamento e facilitar testes

### 4. **Builder Pattern**
**Classes**: `HttpResponse`, `ValidaIpInfo`

```java
HttpResponse.builder()
    .code(200)
    .object(jsonObject)
    .ms(150)
    .message(null)
    .build();
```

**Fornecido por**: Lombok `@Builder`

### 5. **Template Method Pattern**
**Interface**: `CheckService`

```java
public interface CheckService {
    void execute(ConsoleCommandSender console);
}
```

**Implementações**:
- `CheckCreateGroupItem`
- `CheckDelivery`

**Finalidade**: Definir estrutura de checagens na inicialização

### 6. **Observer Pattern**
**Bukkit Event System**: Implementação nativa do padrão Observer

```java
// Listener = Observer
public class ProdutoListener implements Listener {
    @EventHandler // Assina o evento
    public void onPreActive(ProductPreActiveEvent e) { ... }
}

// Event Manager = Subject
Bukkit.getPluginManager().callEvent(new ProductActiveEvent(...));
```

### 7. **Factory Pattern (Implicit)**
**Classe**: `PluginLoadUtil`

```java
// Cria e configura objetos complexos
public void prepareWebServiceConnection(...) {
    ls = new SiteUtil();
    // configurações...
    requestProvider = new RequestProviderImpl(ls);
    provider = new LSProviderImpl(requestProvider, pl);
}
```

### 8. **Data Transfer Object (DTO)**
**Classes**: `ItemInfo`, `ProdutoInfo`, `ValidaIpInfo`

```java
@Data // Lombok gera getters/setters
@ToString
public class ItemInfo {
    private Long entregaID;
    private String player;
    // ...
}
```

**Finalidade**: Transferir dados entre camadas

### 9. **Adapter Pattern**
**Classe**: `ConfigManager extends YamlConfiguration`

```java
// Adapta YamlConfiguration do Bukkit com funcionalidades extras
@Override
public void set(String path, Object obj) {
    super.set(path, obj);
    this.save(); // Auto-save
}
```

---

## ⚙️ Auto-Configuração

### CheckCreateGroupItem

**Trigger**: Executado no `onEnable()` do plugin

**Algoritmo**:
```java
1. gruposNoSite = API.getTodosProdutosDaLoja()
2. gruposLocais = ConfigManager.read("produtos.yml")
3. Para cada grupoNoSite:
   Se grupoNoSite NÃO está em gruposLocais:
      Criar template de configuração
```

**Template Gerado**:
```yaml
Grupos:
  {NomeGrupo}:
    Ativado: false  # Administrador deve ativar manualmente
    Ativar_Com_Player_Offline: false
    Enviar_Mensagem: false
    Mensagem_Receber_Ao_Ativar_Produto:
      - "&eOla &a@player"
      - "&eO produto que voce adquiriu (&a@produto&e) foi ativado!"
      - "&eDias: &a@dias"
      - "&eQuantidade: &a@qnt"
    Money: false
    Quantidade_De_Money: 0
    Cmds_A_Executar:
      - "gerarvip {NomeGrupo} @dias @qnt @player"
```

**Vantagens**:
- Reduz configuração manual
- Evita erros de digitação
- Facilita adição de novos produtos

**Limitações**:
- Grupo criado como `Ativado: false`
- Administrador deve revisar e ativar

---

## 🚚 Sistema de Delivery

### CheckDelivery

**Tipo**: Tarefa assíncrona repetitiva

**Configuração**:
- **Delay Inicial**: 10 segundos (20 ticks * 10)
- **Intervalo**: Configurável via `config.yml` (padrão: 60s)

**Código**:
```java
new BukkitRunnable() {
    public void run() {
        List<ItemInfo> itens = lsProvider.getTodasEntregas(LSEntregaStatus.PENDENTE);
        // processar entregas
    }
}.runTaskTimerAsynchronously(pl, 20*10, 20*pl.getTempoChecarItens());
```

### Validações de Entrega

#### 1. **Validação de Servidor**
```java
private boolean checaServidorCorretoEntregarItem(ItemInfo item, String servidor) {
    return item.getSubServidor().equalsIgnoreCase(servidor);
}
```

**Motivo**: Servidor pode ter múltiplos modos (Factions, RankUP, SkyWars)

#### 2. **Validação de Grupo Configurado**
```java
private boolean checaItemNaConfig(ItemInfo item, Player p) {
    if(!pl.produtoAtivado(item.getGrupo())) {
        // Grupo não está configurado ou não está ativado
        return false;
    }
    return true;
}
```

#### 3. **Validação de Player Online**
```java
private boolean checaEntregarComPlayerOffline(ItemInfo item, Player p) {
    if(p == null) {
        if(!config.getBoolean("Ativar_Com_Player_Offline", false)) {
            // Exceto para grupos DISPUTA/RESOLVIDO
            return false;
        }
    }
    return true;
}
```

#### 4. **Validação de Inventário Vazio**
```java
public static boolean isInventoryEmpty(Player p) {
    for(ItemStack item : p.getInventory().getContents()) {
        if(item != null && item.getType() != Material.AIR)
            return false;
    }
    for(ItemStack item : p.getInventory().getArmorContents()) {
        if(item != null && item.getType() != Material.AIR)
            return false;
    }
    return true;
}
```

**Uso**: Alguns produtos requerem inventário vazio

#### 5. **Validação de Nick Compatível**
```java
private boolean isNickCompativelComEntrega(Player p, ItemInfo itemInfo) {
    return itemInfo.getPlayer().equalsIgnoreCase(p.getName());
}
```

**Motivo**: Prevenir entregas para jogador errado (caso de nick change)

---

## 🛠️ Utilitários (Utils)

### ConfigManager
**Herda**: `YamlConfiguration` (Bukkit)

**Finalidade**: Gerenciar arquivos de configuração YAML

**Métodos Principais**:
```java
public ConfigManager(String name, Plugin plugin)
// Cria/carrega arquivo YAML

@Override
public void set(String path, Object obj)
// Salva automaticamente após set()

public void save()
// Salva no disco

public void reload()
// Recarrega do disco
```

**Uso**:
```java
ConfigManager confGrupos = new ConfigManager("produtos", pl);
confGrupos.set("Grupos.VIP.Ativado", true);
// Auto-salvo!
```

### SiteUtil
**Finalidade**: Armazenar configurações de conexão API

**Propriedades**:
```java
private int connectionTimeout;    // Timeout de conexão (ms)
private int readTimeout;          // Timeout de leitura (ms)
private String credencial;        // SECRET_API
private String ipMaquina;         // IP validado
private String tokenServidor;     // Token do servidor
private String serverRequest;     // URL base da API
private boolean debug;            // Modo debug
```

### DateDuration
**Finalidade**: Medir tempo de execução de requisições HTTP

**Uso**:
```java
DateDuration timer = new DateDuration();
// ... executar operação ...
long ms = timer.calculate(); // tempo decorrido
```

### HttpResponse
**Padrão**: DTO (Data Transfer Object)

**Campos**:
```java
private int code;           // Código HTTP
private JsonElement object; // Response JSON
private long ms;            // Tempo de resposta
private String message;     // Mensagem de erro
```

### PluginLoadUtil
**Finalidade**: Inicializar conexão com API

**Método Principal**:
```java
public void prepareWebServiceConnection(
    ConsoleCommandSender b, 
    String keyapi, 
    ILSProvider provider,
    IRequestProvider requestProvider, 
    SiteUtil ls, 
    LojaSquare pl
)
```

**Fluxo**:
```
1. Cria SiteUtil
2. Configura credenciais e timeouts
3. Cria RequestProviderImpl
4. Cria LSProviderImpl
5. Injeta providers no LojaSquare
```

### TestCall
**Finalidade**: Classe de testes (não utilizada em produção)

---

## 📊 Modelos de Dados (DTOs)

### ItemInfo
**Representa**: Uma entrega de produto

```java
@Data
@ToString
public class ItemInfo {
    private Long entregaID;      // ID da entrega na API
    private String player;       // Nome do jogador
    private String produto;      // Nome do produto
    private String servidor;     // Servidor principal
    private String subServidor;  // Sub-servidor (Factions, etc.)
    private String grupo;        // Grupo de configuração
    private String codigo;       // Código da transação
    private String status;       // "PENDENTE" ou "ENTREGUE"
    private String cupom;        // Código do cupom usado
    private int statusID;        // 1 (PENDENTE) ou 2 (ENTREGUE)
    private int dias;            // Quantidade de dias (VIPs)
    private int quantidade;      // Quantidade comprada
    private long atualizadoEm;   // Timestamp de atualização
}
```

**Serialização**: Gson (JSON ↔ Java)

### ProdutoInfo
**Representa**: Um produto cadastrado na loja

```java
@Data
@ToString
@AllArgsConstructor
public class ProdutoInfo {
    private String grupo;    // Grupo de configuração
    private String produto;  // Nome do produto
}
```

### ValidaIpInfo
**Representa**: Resposta de validação de IP

```java
@Builder
@Data
@AllArgsConstructor
@ToString
public class ValidaIpInfo {
    private boolean sucesso;  // Validação OK?
    private String ip;        // IP da máquina
}
```

### Enums

#### LSEntregaStatus
```java
public enum LSEntregaStatus {
    PENDENTE(1, "PENDENTE"),
    ENTREGUE(2, "ENTREGUE");
    
    @Getter private int code;
    @Getter private String status;
}
```

#### LSResponseEnum
```java
public enum LSResponseEnum {
    SEM_CONEXAO(0, "Servidor sem conexao com a internet"),
    CONEXAO_NAO_AUTORIZADA(401, "Conexao nao autorizada"),
    IP_OU_KEY_BLOQUEADOS(403, "IP ou key bloqueados"),
    NADA_ENCONTRADO(404, "Nenhuma entrega pendente"),
    ASSINATURA_EXPIROU(405, "Assinatura expirada"),
    NADA_MUDOU(406, "Nenhuma atualizacao efetuada"),
    IP_NAO_LIBERADO(409, "IP diferente do cadastrado");
}
```

---

## 🗺️ Mapeamento Completo de Métodos

### LojaSquare (Classe Principal)

#### onEnable()
```java
public void onEnable()
```
- **Trigger**: Plugin ativado pelo Bukkit
- **Responsabilidades**:
  1. Definir variáveis de ambiente
  2. Validar configuração de servidor
  3. Carregar grupos configurados
  4. Preparar conexão WebService
  5. Validar IP da máquina
  6. Registrar eventos e comandos
  7. Executar checagens de inicialização

#### onDisable()
```java
public void onDisable()
```
- **Trigger**: Plugin desativado
- **Responsabilidades**: Exibir mensagem de despedida

#### defineVariaveisAmbiente()
```java
private void defineVariaveisAmbiente()
```
- **Finalidade**: Carregar configurações do `config.yml`
- **Variáveis**:
  - `debug`: Modo debug
  - `servidor`: Nome do servidor
  - `smartDelivery`: Smart delivery ativo?
  - `tempoChecarItens`: Intervalo de checagem

#### checarIPCorreto()
```java
public void checarIPCorreto(ConsoleCommandSender b, String nome)
```
- **Finalidade**: Validar IP da máquina via API
- **Tipo**: Assíncrono (BukkitRunnable)
- **Fluxo**:
  1. Chama `lsProvider.getIpMaquina()`
  2. Se falha ou IP incorreto: desativa plugin
  3. Se sucesso: armazena IP em `SiteUtil`

#### checarServidorConfigurado()
```java
public boolean checarServidorConfigurado(ConsoleCommandSender b)
```
- **Finalidade**: Validar se servidor foi configurado
- **Validação**: `servidor != null && servidor != "Nome-Do-Servidor"`
- **Retorno**: `true` se configurado

#### carregaGruposEntregaConfigurados()
```java
private void carregaGruposEntregaConfigurados(ConsoleCommandSender b)
```
- **Finalidade**: Carregar grupos do `produtos.yml`
- **Lógica**:
  ```
  Para cada grupo em produtos.yml:
    Adiciona a produtosConfigurados[]
    Se Ativado=true:
      Adiciona a produtosAtivados[]
  ```

#### registraEventosCmds()
```java
private void registraEventosCmds()
```
- **Finalidade**: Registrar listeners e comandos
- **Registros**:
  - `ProdutoListener`
  - Comando `/lsite`

#### checagensDeInicializacao()
```java
private void checagensDeInicializacao(ConsoleCommandSender b)
```
- **Finalidade**: Executar serviços de checagem
- **Serviços**:
  1. `CheckCreateGroupItem` (auto-config)
  2. `CheckDelivery` (sistema de entrega)

#### printDebug()
```java
public static void printDebug(String s)
```
- **Finalidade**: Imprimir mensagens de debug
- **Destinos**:
  - Console
  - Players OP
  - Players com `lojasquare.debug`

#### getOnlinePlayers()
```java
public static Player[] getOnlinePlayers()
```
- **Finalidade**: Obter jogadores online (compatibilidade de versões)
- **Técnica**: Reflection para compatibilidade entre versões Bukkit

---

### CmdMain (Comandos)

#### onCommand()
```java
public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args)
```

**Comando**: `/lsite ativar <codigo>`

**Fluxo**:
```
1. Validar sintaxe
2. Verificar se sender é Player
3. Executar assincronamente:
   a. Chamar lsProvider.activateAccount(codigo, player)
   b. Se sucesso: enviar mensagem de sucesso
   c. Se falha: enviar mensagem de erro
```

**Uso**: Ativar conta no painel LojaSquare

---

### LSProviderImpl (Integração API)

#### getTodasEntregas()
```java
public List<ItemInfo> getTodasEntregas(LSEntregaStatus status)
```
- **Endpoint**: `GET /v1/entregas/{status}?status={code}`
- **Retorno**: Lista de `ItemInfo`
- **Tratamento**: Deserialização JSON → ItemInfo via Gson

#### getTodosProdutosDaLoja()
```java
public List<ProdutoInfo> getTodosProdutosDaLoja()
```
- **Endpoint**: `GET /v1/produtos?tokenSubServidor={token}`
- **Retorno**: Lista de `ProdutoInfo`

#### updateDelivery()
```java
public boolean updateDelivery(ItemInfo ii)
```
- **Endpoint**: `PUT /v1/entregas/{entregaID}/entregue`
- **Retorno**: `true` se status 200/201/204

#### activateAccount()
```java
public boolean activateAccount(String codigo, String usuario)
```
- **Endpoint**: `PUT /v1/clientes/activate?codigo={codigo}&usuario={usuario}`
- **Retorno**: `true` se resposta válida

#### getIpMaquina()
```java
public ValidaIpInfo getIpMaquina()
```
- **Endpoint**: `GET /v1/sites/extensoes`
- **Retorno**: `ValidaIpInfo`

---

### RequestProviderImpl (HTTP Client)

#### get()
```java
public HttpResponse get(String url) throws IOException
```
- **Método HTTP**: GET
- **Retorno**: `HttpResponse` com status, JSON, e tempo de resposta

#### put()
```java
public HttpResponse put(String url) throws IOException
```
- **Método HTTP**: PUT
- **Retorno**: `HttpResponse`

#### buildDefaultConnection()
```java
private HttpsURLConnection buildDefaultConnection(HttpsURLConnection c, String method)
```
- **Finalidade**: Configurar headers padrão
- **Headers**:
  - `Authorization`: SECRET_API
  - `Accept`: application/json
  - `Content-Type`: application/json
  - `User-Agent`: Firefox 25.0
- **Timeouts**: Connection e Read

---

## 🔄 Guia de Portabilidade

### Para Adaptar a Outro Jogo/Plataforma

#### 1. **Substituir Dependências do Bukkit**

**Arquivos Afetados**:
- `LojaSquare.java`
- `CmdMain.java`
- `ProdutoListener.java`
- `CheckDelivery.java`
- `ConfigManager.java`

**Componentes a Substituir**:

| Bukkit/Spigot | Substituto Genérico | Finalidade |
|---------------|---------------------|------------|
| `JavaPlugin` | Classe de inicialização do framework | Entry point |
| `Player` | Classe de jogador da plataforma | Representar jogador |
| `Bukkit.getPlayer()` | API de busca de jogador | Buscar jogador online |
| `Bukkit.dispatchCommand()` | Sistema de comandos | Executar comandos |
| `BukkitRunnable` | Sistema de threading | Tarefas async/sync |
| `YamlConfiguration` | Biblioteca YAML (SnakeYAML) | Ler configs |
| `Inventory` | Sistema de inventário do jogo | Gerenciar items |
| `Event/Listener` | Sistema de eventos do framework | Event-driven |

#### 2. **Manter Camadas Independentes**

**Não precisam mudar**:
- ✅ `providers/` (ILSProvider, IRequestProvider)
- ✅ `utils/model/` (ItemInfo, ProdutoInfo, etc.)
- ✅ `utils/enums/` (LSEntregaStatus, LSResponseEnum)
- ✅ `utils/HttpResponse`, `utils/DateDuration`, `utils/SiteUtil`

**Razão**: São agnósticos de plataforma

#### 3. **Criar Abstrações**

**Exemplo**: Interface de Jogador

```java
// Abstração
public interface IPlayer {
    String getName();
    void sendMessage(String msg);
    boolean isOnline();
    boolean hasPermission(String perm);
}

// Implementação Bukkit
public class BukkitPlayer implements IPlayer {
    private Player bukkitPlayer;
    
    public String getName() {
        return bukkitPlayer.getName();
    }
    // ...
}

// Implementação Sponge (outro framework Minecraft)
public class SpongePlayer implements IPlayer {
    private org.spongepowered.api.entity.living.player.Player spongePlayer;
    
    public String getName() {
        return spongePlayer.getName();
    }
    // ...
}
```

#### 4. **Adaptar Sistema de Eventos**

**Estrutura Atual** (Bukkit):
```java
public class ProductActiveEvent extends Event implements Cancellable {
    // Bukkit-specific
}

@EventHandler
public void onActive(ProductActiveEvent e) { ... }
```

**Estrutura Genérica**:
```java
// Interface de evento
public interface IProductActiveEvent {
    IPlayer getPlayer();
    ItemInfo getItemInfo();
    boolean isCancelled();
    void setCancelled(boolean cancel);
}

// Interface de listener
public interface IEventListener {
    void onProductPreActive(IProductPreActiveEvent event);
    void onProductActive(IProductActiveEvent event);
}
```

#### 5. **Adaptar Sistema de Configuração**

Se a plataforma não usa YAML:

```java
// Interface genérica
public interface IConfigManager {
    String getString(String path);
    boolean getBoolean(String path);
    int getInt(String path);
    List<String> getStringList(String path);
    void set(String path, Object value);
    void save();
}

// Implementação JSON
public class JsonConfigManager implements IConfigManager {
    // Usar Gson para JSON
}

// Implementação TOML
public class TomlConfigManager implements IConfigManager {
    // Usar biblioteca TOML
}
```

#### 6. **Checklist de Portabilidade**

- [ ] Identificar todas as chamadas `org.bukkit.*`
- [ ] Criar interfaces abstratas para jogadores
- [ ] Criar interfaces abstratas para comandos
- [ ] Criar sistema de eventos genérico
- [ ] Adaptar sistema de configuração
- [ ] Adaptar sistema de agendamento (scheduler)
- [ ] Testar em ambiente sandbox da nova plataforma

---

## 📚 Referências Técnicas

### Documentação Externa
- **Bukkit/Spigot API**: https://hub.spigotmc.org/javadocs/spigot/
- **LojaSquare API**: https://github.com/TrowDev/Teste-API-Loja-Square
- **Painel LojaSquare**: https://painel.lojasquare.net/

### Configurações Importantes

**config.yml**:
```yaml
LojaSquare:
  SECRET_API: "sua-chave-aqui"
  Servidor: "Nome-Do-Servidor"
  Token_Servidor: "token-do-servidor"
  Connection_Timeout: 10000
  Read_Timeout: 10000
  
Config:
  Tempo_Checar_Compras: 60
```

**produtos.yml** (exemplo):
```yaml
Grupos:
  VIPDiamante:
    Ativado: true
    Ativar_Com_Player_Offline: false
    Enviar_Mensagem: true
    Mensagem_Receber_Ao_Ativar_Produto:
      - "&eParabens &a@player"
      - "&eSeu &bVIP Diamante &efoi ativado!"
    Money: false
    Quantidade_De_Money: 0
    Cmds_A_Executar:
      - "lp user @player parent set vipdiamante"
      - "lp user @player permission set vip.diamante true"
```

---

## 🏆 Conclusão

Este documento mapeia **completamente** a arquitetura, componentes, lógicas de negócio e integração do **LojaSquare Plugin v2.0**.

**Principais Características**:
- ✅ **Plugin assíncrono** para não travar o servidor
- ✅ **API RESTful** segura com autenticação
- ✅ **Sistema de eventos** extensível
- ✅ **Auto-configuração** de novos produtos
- ✅ **Smart Delivery** para otimizar performance
- ✅ **Validações robustas** antes de entregar
- ✅ **Design patterns** modernos (Strategy, Observer, Builder, etc.)

**Para portar para outra plataforma**:
1. Manter camadas `providers`, `utils/model`, `utils/enums`
2. Substituir dependências Bukkit por abstrações
3. Adaptar sistema de eventos
4. Adaptar sistema de configuração
5. Testar integração com API LojaSquare

---

**Desenvolvido por**: TrowDev  
**Licença**: Proprietária - LojaSquare  
**Versão**: 2.0-SNAPSHOT  
**Última Atualização**: 2026-01-19
