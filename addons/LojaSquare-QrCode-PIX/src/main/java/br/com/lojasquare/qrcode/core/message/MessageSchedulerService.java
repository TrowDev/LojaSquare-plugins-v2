package br.com.lojasquare.qrcode.core.message;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.core.CheckService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
@AllArgsConstructor
public class MessageSchedulerService implements CheckService {
    private LojaSquare pl;

    @Override
    public void execute(ConsoleCommandSender console) {
        if(!pl.getConfig().getBoolean("Config.Anuncio.Ativar")) return;
        new BukkitRunnable(){
            @Override
            public void run() {
                for(String s : pl.getMensagensAnuncio()) {
                    Bukkit.broadcastMessage(s);
                }
            }
        }.runTaskTimerAsynchronously(pl, 20 * 30,20 * pl.getConfig().getInt("Config.Anuncio.Tempo_Repetir_Anuncio", 60));
    }
}
