package net.ceyzel.parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ParkourListener implements Listener {
    private final CeyZelParkour plugin;
    private final Set<UUID> checkpointedPlayers = new HashSet<>();

    public ParkourListener(CeyZelParkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ParkourSession session = plugin.getActiveSessions().get(player.getUniqueId());
        if (session != null) {
            event.setKeepInventory(true);
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.spigot().respawn();
                Location checkpointLocation = session.getLastCheckpointLocation();
                player.teleport(checkpointLocation);
            });
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ParkourSession session = plugin.getActiveSessions().get(player.getUniqueId());

        if (session != null) {
            Block toBlock = event.getTo().getBlock();
            ParkourMap map = plugin.getMap(session.getMapName());

            if (map != null) {
                if (map.getFinish() != null && map.getFinish().equals(toBlock)) {
                    Duration time = plugin.getParkourTimer().getElapsedTime(player.getUniqueId());
                    plugin.addPlayerScore(player.getUniqueId(), map.getDifficulty().getScore());
                    plugin.addMapCompletion(player.getUniqueId(), map.getName(), time.toMillis()); // Store time in milliseconds
                    player.sendMessage("Карта пройдена, вы получаете " + map.getDifficulty().getScore() + " очков. Время: " + plugin.getParkourTimer().formatDuration(time));
                    session.endSession();
                    plugin.getActiveSessions().remove(player.getUniqueId());
                    checkpointedPlayers.remove(player.getUniqueId());
                    Location lobbyLocation = plugin.getLobbyLocation();
                    if (lobbyLocation != null) {
                        player.teleport(lobbyLocation);
                    }
                    return;
                }

                if (map.getCheckpoints() != null && map.getCheckpoints().contains(toBlock)) {
                    if (!checkpointedPlayers.contains(player.getUniqueId())) {
                        session.setLastCheckpoint(toBlock);
                        player.sendMessage("Чекпоинт сохранен!");
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        checkpointedPlayers.add(player.getUniqueId());
                    }
                }
            }
        }
    }
}