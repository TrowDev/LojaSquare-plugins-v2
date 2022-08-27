package br.com.lojasquare.commands;

import br.com.lojasquare.LojaSquare;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@AllArgsConstructor
public class CmdMain implements CommandExecutor {

    @Getter private LojaSquare pl;
    @Getter private ILSProvider lsProvider;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(s.equalsIgnoreCase("lsite")) {
            if(args.length == 0) {
                sender.sendMessage(pl.getMsg("Msg.Use_Cmd_LSite"));
                return false;
            }
            if(args[0].equalsIgnoreCase("ativar")) {
                if(args.length < 2) {
                    sender.sendMessage(pl.getMsg("Msg.Use_Cmd_LSite"));
                    return false;
                }
                String codigo = args[1];
                if(lsProvider.activateAccount(codigo)) {
                    sender.sendMessage(pl.getMsg("Msg.Conta_Ativada"));
                } else {
                    sender.sendMessage(pl.getMsg("Msg.Erro_Ao_Ativar_Conta"));
                }
            }
            return true;
        }
        return false;
    }
}
