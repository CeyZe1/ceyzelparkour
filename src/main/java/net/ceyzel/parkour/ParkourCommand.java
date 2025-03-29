package net.ceyzel.parkour;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ParkourCommand implements CommandExecutor {
    private final CeyZelParkour plugin;

    public ParkourCommand(CeyZelParkour plugin) {
        this.plugin = plugin;
    }

    public static LiteralCommandNode coolJoinCommand(CeyZelParkour plugin) {
        return Commands.literal("join")
                .requires(ctx -> ctx.getSender().hasPermission("ceyzel.join"))
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage("Use /join <map>");
                    return 0;
                })
                .then(Commands.argument("map", StringArgumentType.string()).suggests((ctx, builder)->{
                    plugin.getParkourMaps().keySet()
                            .stream()
                            .filter(entry->entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                            .forEach(builder::suggest);
                    return builder.buildFuture();
                }).executes(ctx -> {
                            if(ctx.getSource().getSender() instanceof Player player) {
                                ParkourMap map = plugin.getParkourMaps().get(ctx.getArgument("map", String.class));
                                if (map == null) {
                                    player.sendMessage(ChatColor.RED + "Карта не найдена!");
                                    return 0;
                                }

                                if (map.getStart() == null) {
                                    player.sendMessage(ChatColor.RED + "Стартовая точка не установлена!");
                                    return 0;
                                }

                                UUID playerId = player.getUniqueId();
                                if (plugin.getActiveSessions().containsKey(playerId)) {
                                    player.sendMessage(ChatColor.RED + "Вы уже на карте");
                                    return 0;
                                }

                                player.teleport(map.getStart().getLocation());
                                ParkourSession session = new ParkourSession(playerId, map.getName(), map.getStart());
                                plugin.getActiveSessions().put(playerId, session);
                                player.sendMessage(ChatColor.GREEN + "Вы зашли на карту");
                                return 1;
                            }
                            return 0;
                        }
                )).build();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreateCommand(sender, args);
                break;
            case "setstart":
                handleSetStartCommand(sender, args);
                break;
            case "setcheckpoint":
                handleSetCheckpointCommand(sender, args);
                break;
            case "setfinish":
                handleSetFinishCommand(sender, args);
                break;
            case "setscore":
                handleSetScoreCommand(sender, args);
                break;
            case "remove":
                handleRemoveCommand(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "CeyzelParkour Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/ceyzel create <название> " + ChatColor.GRAY + "- Создать карту");
        sender.sendMessage(ChatColor.YELLOW + "/ceyzel setstart <название> " + ChatColor.GRAY + "- Установить старт");
        sender.sendMessage(ChatColor.YELLOW + "/ceyzel setcheckpoint <название> " + ChatColor.GRAY + "- Добавить чекпоинт");
        sender.sendMessage(ChatColor.YELLOW + "/ceyzel setfinish <название> " + ChatColor.GRAY + "- Установить финиш");
        sender.sendMessage(ChatColor.YELLOW + "/ceyzel setscore <название> <очки> " + ChatColor.GRAY + "- Назначить награду");
        sender.sendMessage(ChatColor.YELLOW + "/join <название> " + ChatColor.GRAY + "- Начать паркур");
        sender.sendMessage(ChatColor.YELLOW + "/ceyzel mapinfo <название> " + ChatColor.GRAY + "- Информация о карте");
        sender.sendMessage(ChatColor.YELLOW + "/ceyzel remove <название> " + ChatColor.GRAY + "- Удалить карту");
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ceyzelparkour.admin")) return;
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Используйте: /ceyzel create <название>");
            return;
        }

        String mapName = args[1];
        if (plugin.getParkourMaps().containsKey(mapName)) {
            sender.sendMessage(ChatColor.RED + "Карта с таким названием уже существует!");
            return;
        }

        sender.sendMessage("1");
        ParkourMap newMap = new ParkourMap(mapName);
        sender.sendMessage("1");
        plugin.getParkourMaps().put(mapName, newMap);
        sender.sendMessage("1");
        plugin.saveMap(newMap);
        sender.sendMessage(ChatColor.GREEN + "Карта '" + mapName + "' создана!");
    }

    private void handleJoinCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            return;
        }
        if (!checkPermission(sender, "ceyzelparkour.join")) return;

        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Используйте: /join <название>");
            return;
        }

        ParkourMap map = plugin.getParkourMaps().get(args[0]);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Карта не найдена!");
            return;
        }

        if (map.getStart() == null) {
            player.sendMessage(ChatColor.RED + "Стартовая точка не установлена!");
            return;
        }

        UUID playerId = player.getUniqueId();
        if (plugin.getActiveSessions().containsKey(playerId)) {
            player.sendMessage(ChatColor.RED + "Вы уже на карте");
            return;
        }

        player.teleport(map.getStart().getLocation());
        ParkourSession session = new ParkourSession(playerId, map.getName(), map.getStart());
        plugin.getActiveSessions().put(playerId, session);
        player.sendMessage(ChatColor.GREEN + "Вы зашли на карту");
    }

    private void handleSetStartCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            return;
        }
        if (!checkPermission(sender, "ceyzelparkour.admin")) return;

        Player player = (Player) sender;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Используйте: /ceyzel setstart <название>");
            return;
        }

        ParkourMap map = plugin.getParkourMaps().get(args[1]);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Карта не найдена!");
            return;
        }

        Block block = player.getLocation().getBlock();
        block.setType(Material.STONE_PRESSURE_PLATE); // Устанавливаем плиту
        map.setStart(block);
        plugin.saveMap(map);
        player.sendMessage(ChatColor.GREEN + "Стартовая точка установлена!");
    }

    private void handleSetCheckpointCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            return;
        }
        if (!checkPermission(sender, "ceyzelparkour.admin")) return;

        Player player = (Player) sender;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Используйте: /ceyzel setcheckpoint <название>");
            return;
        }

        ParkourMap map = plugin.getParkourMaps().get(args[1]);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Карта не найдена!");
            return;
        }

        Block block = player.getLocation().getBlock();
        block.setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE); // Устанавливаем плиту
        map.addCheckpoint(block);
        plugin.saveMap(map);
        player.sendMessage(ChatColor.GREEN + "Чекпоинт добавлен!");
    }

    private void handleSetFinishCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            return;
        }
        if (!checkPermission(sender, "ceyzelparkour.admin")) return;

        Player player = (Player) sender;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Используйте: /ceyzel setfinish <название>");
            return;
        }

        ParkourMap map = plugin.getParkourMaps().get(args[1]);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Карта не найдена!");
            return;
        }

        Block block = player.getLocation().getBlock();
        block.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE); // Устанавливаем плиту
        map.setFinish(block);
        plugin.saveMap(map);
        player.sendMessage(ChatColor.GREEN + "Финишная точка установлена!");
    }

    private void handleSetScoreCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ceyzelparkour.admin")) return;
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Используйте: /ceyzel setscore <название> <очки>");
            return;
        }

        try {
            double score = Double.parseDouble(args[2]);
            ParkourMap map = plugin.getParkourMaps().get(args[1]);
            if (map == null) {
                sender.sendMessage(ChatColor.RED + "Карта не найдена!");
                return;
            }

            map.setScore(score);
            plugin.saveMap(map);
            sender.sendMessage(ChatColor.GREEN + "Награда установлена: " + score + " очков");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Некорректное число очков!");
        }
    }

    private void handleMapInfoCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
            return;
        }

        Player player = (Player) sender;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Используйте: /ceyzel mapinfo <название>");
            return;
        }

        ParkourMap map = plugin.getParkourMaps().get(args[1]);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Карта не найдена!");
            return;
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
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "Недостаточно прав!");
            return false;
        }
        return true;
    }

    private void handleRemoveCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "ceyzelparkour.admin")) return;
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Используйте: /ceyzel remove <название>");
            return;
        }

        String mapName = args[1];
        if (!plugin.getParkourMaps().containsKey(mapName)) {
            sender.sendMessage(ChatColor.RED + "Карта с таким названием не найдена!");
            return;
        }

        plugin.getParkourMaps().remove(mapName);
        plugin.getConfig().set(mapName, null); // Удаляем карту из конфига
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Карта '" + mapName + "' удалена!");
    }
}