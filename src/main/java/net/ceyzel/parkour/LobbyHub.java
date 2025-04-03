package net.ceyzel.parkour;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LobbyHub {
    public static void registerCommands(CeyZelParkour plugin) {
        BasicCommand lobby = (commandSourceStack, strings) -> {
            if (commandSourceStack.getExecutor() instanceof Player player) {
                Location lobbyLocation = plugin.getLobbyLocation();
                if (lobbyLocation != null) {
                    player.teleport(lobbyLocation);
                } else {
                    player.sendMessage("Локация лобби не найдена!");
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
                        Location checkpointLocation = session.getLastCheckpointLocation();
                        if (checkpointLocation != null) {
                            executor.teleport(checkpointLocation);
                        } else {
                            executor.sendMessage("Чекпоинт не найден!");
                        }
                    } else {
                        executor.sendMessage("Ты не на карте!");
                    }
                }
            }));
        });
    }
}