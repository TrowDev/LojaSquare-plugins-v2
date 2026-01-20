package br.com.lojasquare.commands;

import java.util.concurrent.CompletableFuture;

import br.com.lojasquare.LojaSquarePlugin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.lojasquare.providers.lojasquare.ILSProvider;

/**
 * Comando /lsite para ativação de contas.
 * Equivalente ao CmdMain do Spigot, adaptado para Hytale.
 * 
 * Uso: /lsite ativar <codigo>
 */
@AllArgsConstructor
public class CmdLSite {

    @Getter
    private final LojaSquarePlugin plugin;

    @Getter
    private final ILSProvider lsProvider;

    /**
     * Processa o comando /lsite.
     * 
     * @param senderName Nome de quem executou o comando
     * @param senderUUID UUID de quem executou (null se for console)
     * @param isPlayer   Se o sender é um jogador
     * @param args       Argumentos do comando
     * @return true se o comando foi processado com sucesso
     */
    public boolean onCommand(String senderName, java.util.UUID senderUUID, boolean isPlayer, String[] args) {
        if (args.length == 0) {
            sendMessage(senderUUID, plugin.getMsg("Msg.Use_Cmd_LSite"));
            return false;
        }

        if (args[0].equalsIgnoreCase("ativar")) {
            if (args.length < 2) {
                sendMessage(senderUUID, plugin.getMsg("Msg.Use_Cmd_LSite"));
                return false;
            }

            if (!isPlayer) {
                sendMessage(senderUUID, "§cComando exclusivo para jogadores.");
                return false;
            }

            String codigo = args[1];

            // Executa assincronamente
            CompletableFuture.runAsync(() -> {
                if (lsProvider.activateAccount(codigo, senderName)) {
                    sendMessage(senderUUID, plugin.getMsg("Msg.Conta_Ativada"));
                } else {
                    sendMessage(senderUUID, plugin.getMsg("Msg.Erro_Ao_Ativar_Conta"));
                }
            });

            return true;
        }

        sendMessage(senderUUID, plugin.getMsg("Msg.Use_Cmd_LSite"));
        return false;
    }

    private void sendMessage(java.util.UUID playerUUID, String message) {
        if (playerUUID != null) {
            plugin.sendMessageToPlayer(playerUUID, message);
        } else {
            plugin.log(message);
        }
    }
}
