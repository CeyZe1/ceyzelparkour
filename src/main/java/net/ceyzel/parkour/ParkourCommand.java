package net.ceyzel.parkour;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ParkourCommand {
    private final CeyZelParkour plugin;

    public ParkourCommand(CeyZelParkour plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Suggestions> suggestMaps(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        plugin.getMaps().keySet().stream()
                .filter(entry -> entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    public LiteralCommandNode<CommandSourceStack> createCeyzelCommand() {
        return Commands.literal("ceyzel")
                .requires(stack -> stack.getSender().hasPermission("ceyzelparkour.admin"))
                .executes(ctx -> {
                    var sender = ctx.getSource().getSender();
                    sender.sendMessage(ChatColor.GOLD + "CeyzelParkour Commands:");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel create <name> " + ChatColor.GRAY + "- Создать карту");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel setstart <name> " + ChatColor.GRAY + "- Поставить старт");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel setcheckpoint <name> " + ChatColor.GRAY + "- Поставишь чекпоинт");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel setfinish <name> " + ChatColor.GRAY + "- Поставить финиш");
                    sender.sendMessage(ChatColor.YELLOW + "/join <name> " + ChatColor.GRAY + "- Зайти на карту");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel mapinfo <name> " + ChatColor.GRAY + "- Информация о карте");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel remove <name> " + ChatColor.GRAY + "- Удалить карту");
                    return 1;
                })
                .then(createMapCommand())
                .then(setStartCommand())
                .then(setCheckpointCommand())
                .then(setFinishCommand())
                .then(removeMapCommand())
                .build(); // Преобразуем LiteralArgumentBuilder в LiteralCommandNode
    }

    public LiteralCommandNode<CommandSourceStack> createJoinCommand() {
        return Commands.literal("join")
                .requires(ctx -> ctx.getSender().hasPermission("ceyzel.join"))
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage("Используй /join <название>");
                    return 0;
                })
                .then(Commands.argument("map", StringArgumentType.string())
                        .suggests(this::suggestMaps)
                        .executes(ctx -> {
                            if (ctx.getSource().getSender() instanceof Player player) {
                                ParkourMap map = plugin.getMap(ctx.getArgument("map", String.class));
                                if (map == null) {
                                    player.sendMessage(ChatColor.RED + "Карта не найдена");
                                    return 0;
                                }

                                if (map.getStart() == null) {
                                    player.sendMessage(ChatColor.RED + "Стартовая точка не найдена");
                                    return 0;
                                }

                                UUID playerId = player.getUniqueId();
                                if (plugin.getActiveSessions().containsKey(playerId)) {
                                    player.sendMessage(ChatColor.RED + "Ты уже на карте");
                                    return 0;
                                }

                                player.teleport(map.getStart().getLocation());
                                ParkourSession session = new ParkourSession(playerId, map.getName(), map.getStart());
                                plugin.getActiveSessions().put(playerId, session);
                                player.sendMessage(ChatColor.GREEN + "Ты зашел на карту");
                                return 1;
                            }
                            return 0;
                        })).build();
    }

    private LiteralCommandNode<CommandSourceStack> createMapCommand() {
        return Commands.literal("create")
                .then(Commands.argument("map", StringArgumentType.string()).executes(ctx -> {
                    var mapName = ctx.getArgument("map", String.class);
                    if (plugin.getMaps().containsKey(mapName)) {
                        ctx.getSource().getSender().sendMessage("Карта с таким именем уже существует");
                        return 0;
                    }

                    ParkourMap newMap = new ParkourMap(mapName);
                    plugin.getMaps().put(mapName, newMap);
                    plugin.saveMap(newMap);
                    ctx.getSource().getSender().sendMessage(ChatColor.GREEN + "Карта '" + mapName + "' добавлена");
                    return 1;
                })).build();
    }

    private LiteralCommandNode<CommandSourceStack> setStartCommand() {
        return Commands.literal("setstart")
                .then(Commands.argument("map", StringArgumentType.string())
                        .suggests(this::suggestMaps)
                        .executes(ctx -> {
                            var mapName = ctx.getArgument("map", String.class);
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Только игрок может использовать эту команду");
                                return 0;
                            }

                            ParkourMap map = plugin.getMap(mapName);
                            if (map == null) {
                                player.sendMessage(ChatColor.RED + "Карта не найдена");
                                return 0;
                            }

                            Location location = player.getLocation();
                            location.setX(Math.round(location.getX() * 10) / 10.0);
                            location.setY(Math.round(location.getY() * 10) / 10.0);
                            location.setZ(Math.round(location.getZ() * 10) / 10.0);
                            location.setYaw((float) (Math.round(location.getYaw() * 10) / 10.0));
                            location.setPitch((float) (Math.round(location.getPitch() * 10) / 10.0));

                            Block block = location.getBlock();
                            block.setType(Material.STONE_PRESSURE_PLATE);
                            map.setStart(block);
                            plugin.saveMap(map);
                            player.sendMessage(ChatColor.GREEN + "Стартовая точка установлена");
                            return 1;
                        })).build();
    }

    private LiteralCommandNode<CommandSourceStack> setCheckpointCommand() {
        return Commands.literal("setcheckpoint")
                .then(Commands.argument("map", StringArgumentType.string())
                        .suggests(this::suggestMaps)
                        .executes(ctx -> {
                            var mapName = ctx.getArgument("map", String.class);
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду");
                                return 0;
                            }

                            ParkourMap map = plugin.getMap(mapName);
                            if (map == null) {
                                player.sendMessage(ChatColor.RED + "Карта не найдена");
                                return 0;
                            }

                            Location location = player.getLocation();
                            location.setX(Math.round(location.getX() * 10) / 10.0);
                            location.setY(Math.round(location.getY() * 10) / 10.0);
                            location.setZ(Math.round(location.getZ() * 10) / 10.0);
                            location.setYaw((float) (Math.round(location.getYaw() * 10) / 10.0));
                            location.setPitch((float) (Math.round(location.getPitch() * 10) / 10.0));

                            Block block = location.getBlock();
                            block.setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
                            map.addCheckpoint(block);
                            plugin.saveMap(map);
                            player.sendMessage(ChatColor.GREEN + "Чекпоинт добавлен!");
                            return 1;
                        })).build();
    }

    private LiteralCommandNode<CommandSourceStack> setFinishCommand() {
        return Commands.literal("setfinish")
                .then(Commands.argument("map", StringArgumentType.string())
                        .suggests(this::suggestMaps)
                        .executes(ctx -> {
                            var mapName = ctx.getArgument("map", String.class);
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду");
                                return 0;
                            }

                            ParkourMap map = plugin.getMap(mapName);
                            if (map == null) {
                                player.sendMessage(ChatColor.RED + "Карта не найдена");
                                return 0;
                            }

                            Block block = player.getLocation().getBlock();
                            block.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
                            map.setFinish(block);
                            plugin.saveMap(map);
                            player.sendMessage(ChatColor.GREEN + "Финишная точка установлена");
                            return 1;
                        })).build();
    }

    private LiteralCommandNode<CommandSourceStack> removeMapCommand() {
        return Commands.literal("remove")
                .then(Commands.argument("map", StringArgumentType.string())
                        .suggests(this::suggestMaps)
                        .executes(ctx -> {
                            var mapName = ctx.getArgument("map", String.class);
                            ParkourMap map = plugin.getMap(mapName);
                            if (map == null) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Карта не найдена");
                                return 0;
                            }

                            plugin.getMaps().remove(mapName);
                            plugin.getConfig().set(mapName, null);
                            plugin.saveConfig();
                            ctx.getSource().getSender().sendMessage(ChatColor.GREEN + "Карта '" + mapName + "' удалена");
                            return 1;
                        })).build();
    }
}