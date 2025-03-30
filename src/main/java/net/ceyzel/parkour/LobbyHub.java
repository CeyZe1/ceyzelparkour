package net.ceyzel.parkour;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LobbyHub {
    public static void registerCommands(CeyZelParkour plugin) {
        BasicCommand lobby = (commandSourceStack, strings) -> {
            if (commandSourceStack.getExecutor() instanceof Player player) {
                Location lobbyLocation = plugin.getLobbyLocation(); // Используем геттер
                if (lobbyLocation != null) {
                    player.teleport(lobbyLocation);
                } else {
                    player.sendMessage("Лобби локейшион не найдено!");
                }
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
                        executor.sendMessage("Ты не на карте!");
                    }
                }
            }));
        });
    }
}