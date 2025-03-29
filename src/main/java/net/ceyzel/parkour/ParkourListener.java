package net.ceyzel.parkour;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;

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
                player.teleport(session.getLastCheckpoint().getLocation());
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
                // Проверка на финиш
                if (map.getFinish() != null && map.getFinish().equals(toBlock)) {
                    long time = (System.currentTimeMillis() - session.getStartTime()) / 1000;
                    plugin.addPlayerScore(player.getUniqueId(), map.getScore());
                    plugin.addMapCompletion(player.getUniqueId(), map.getName(), time);
                    player.sendMessage("Карта пройдена, вы получаете " + map.getScore() + " поинтов. Время: " + time + " сек.");
                    plugin.getActiveSessions().remove(player.getUniqueId());
                    if (plugin.lobby_location != null) {
                        player.teleport(plugin.lobby_location);
                    }
                    return;
                }

                // Проверка на чекпоинт
                if (map.getCheckpoints() != null && map.getCheckpoints().contains(toBlock)) {
                    var steppedOn = map.getCheckpoints().indexOf(toBlock);
                    var key = new NamespacedKey(plugin, "current_checkpoint");
                    var playerPDC = player.getPersistentDataContainer();
                    var current = playerPDC.get(key, PersistentDataType.INTEGER);
                    if (current == null || steppedOn != current) return;
                    playerPDC.set(key, PersistentDataType.INTEGER, current + 1);
                    session.setLastCheckpoint(toBlock);
                    player.sendMessage("Чекпоинт сохранен!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }
        }
    }
}