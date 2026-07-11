package br.com.lojasquare.command;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import br.com.lojasquare.LojasquareMod;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class LojasquareCommands {

    private final LojasquareMod mod;
    private final ILSProvider lsProvider;

    public LojasquareCommands(LojasquareMod mod, ILSProvider lsProvider) {
        this.mod = mod;
        this.lsProvider = lsProvider;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher,
                         CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(
            CommandManager.literal("lsite")
                .then(CommandManager.literal("ativar")
                    .then(CommandManager.argument("codigo", StringArgumentType.word())
                        .executes(this::ativar)
                    )
                )
                .executes(this::help)
        );
    }

    private int ativar(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("§cComando exclusivo para jogadores."));
            return 0;
        }

        String codigo = StringArgumentType.getString(ctx, "codigo");

        CompletableFuture.runAsync(() -> {
            if (lsProvider.activateAccount(codigo, player.getName().getString())) {
                player.sendMessage(Text.literal(mod.getMsg("Msg.Conta_Ativada")));
            } else {
                player.sendMessage(Text.literal(mod.getMsg("Msg.Erro_Ao_Ativar_Conta")));
            }
        });

        return 1;
    }

    private int help(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        source.sendFeedback(() -> Text.literal(mod.getMsg("Msg.Use_Cmd_LSite")), false);
        return 1;
    }
}
