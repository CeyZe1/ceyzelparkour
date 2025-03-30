package net.ceyzel.parkour;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
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
                if (commandSourceStack.getExecutor() instanceof Player executor) {
                    ParkourSession session = plugin.getActiveSessions().get(executor.getUniqueId());
                    if (session != null) {
                        executor.teleport(session.getLastCheckpoint().getLocation());
                    } else {
                        executor.sendMessage("Вы не на карте!");
                    }
                }
            }));
        });
    }
}

