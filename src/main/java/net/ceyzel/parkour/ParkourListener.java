package net.ceyzel.parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ParkourListener implements Listener {
    private final CeyZelParkour plugin;

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
                player.teleport(session.getLastCheckpoint());
            });
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ParkourSession session = plugin.getActiveSessions().get(player.getUniqueId());

        if (session != null && event.getTo().getBlock().getType() == Material.STONE_PRESSURE_PLATE) {
            ParkourMap map = plugin.getMap(session.getMapName());

            if (map != null && map.getFinish() != null && map.getFinish().equals(event.getTo())) {
                plugin.addPlayerScore(player.getUniqueId(), map.getScore());
                player.sendMessage("Карта пройдена, вы получаете " + map.getScore() + " поинтов");
                plugin.getActiveSessions().remove(player.getUniqueId());
            }

            // Если игрок наступил на чекпоинт
            if (map != null && map.getCheckpoints() != null) {
                for (Location checkpoint : map.getCheckpoints()) {
                    if (checkpoint != null && checkpoint.equals(event.getTo())) {
                        session.setLastCheckpoint(checkpoint);
                        player.sendMessage("Чекпоинт сохранен!");
                        break;
                    }
                }
            }
        }
    }
}