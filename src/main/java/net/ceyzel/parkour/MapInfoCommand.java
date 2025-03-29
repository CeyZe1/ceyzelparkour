package net.ceyzel.parkour;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MapInfoCommand implements CommandExecutor {
    private final CeyZelParkour plugin;

    public MapInfoCommand(CeyZelParkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Используйте: /mapinfo <название>");
            return true;
        }

        ParkourMap map = plugin.getParkourMaps().get(args[0]);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Карта не найдена!");
            return true;
        }

        UUID playerId = player.getUniqueId();
        double totalScore = plugin.getPlayerTotalScore(playerId);
        int completions = plugin.getMapCompletions(playerId, map.getName());
        long bestTime = plugin.getBestTime(playerId, map.getName());

        player.sendMessage(ChatColor.GOLD + "Информация о карте '" + map.getName() + "':");
        player.sendMessage(ChatColor.YELLOW + "Награда: " + map.getScore() + " очков");
        player.sendMessage(ChatColor.YELLOW + "Ваш общий счет: " + totalScore + " очков");
        player.sendMessage(ChatColor.YELLOW + "Количество прохождений: " + completions);
        player.sendMessage(ChatColor.YELLOW + "Лучшее время: " + (bestTime == Long.MAX_VALUE ? "Нет данных" : bestTime + " сек"));
        return true;
    }
}