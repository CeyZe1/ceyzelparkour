package net.ceyzel.parkour;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LobbyHub {
    public static void RegisterCommands(CeyZelParkour plugin) {
        BasicCommand lobby = (commandSourceStack, strings) -> {
            if (plugin.lobby_location != null) {  // Add null check
                commandSourceStack.getExecutor().teleport(plugin.lobby_location);
            } else {
                commandSourceStack.getExecutor().sendMessage("Lobby location is not set!");
            }
        };

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            var reg = commands.registrar();
            reg.register("hub", lobby);
            reg.register("lobby", lobby);
            reg.register("kill", ((commandSourceStack, args) -> {
                if (commandSourceStack.getExecutor() instanceof Player executor &&
                        executor.getWorld().equals(Bukkit.getWorld("maps"))) {
                    executor.setHealth(0);
                }
            }));
        });
    }
}
