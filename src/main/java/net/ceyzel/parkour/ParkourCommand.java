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

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ParkourCommand {
    private final CeyZelParkour plugin;

    public ParkourCommand(CeyZelParkour plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Suggestions> mapsSuggestions(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        plugin.getMaps().keySet().stream()
                .filter(entry -> entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    public LiteralCommandNode<CommandSourceStack> coolCeyzelCommand() {
        var builder = Commands.literal("ceyzel");
        builder
                .requires(stack -> stack.getSender().hasPermission("ceyzelparkour.admin"))
                .executes(ctx -> {
                    var sender = ctx.getSource().getSender();
                    sender.sendMessage(ChatColor.GOLD + "CeyzelParkour Commands:");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel create <название> " + ChatColor.GRAY + "- Создать карта");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel setstart <название> " + ChatColor.GRAY + "- Установить старт");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel setcheckpoint <название> " + ChatColor.GRAY + "- Добавить чекпоинт");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel setfinish <название> " + ChatColor.GRAY + "- Установить финиш");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel setscore <название> <очки> " + ChatColor.GRAY + "- Назначить награду");
                    sender.sendMessage(ChatColor.YELLOW + "/join <название> " + ChatColor.GRAY + "- Начать паркур");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel mapinfo <название> " + ChatColor.GRAY + "- Информация о карте");
                    sender.sendMessage(ChatColor.YELLOW + "/ceyzel remove <название> " + ChatColor.GRAY + "- Удалить карту");
                    return 1;
                })
                .then(coolCreateCommand())
                .then(coolSetStartCommand())
                .then(coolSetCheckpointCommand())
                .then(coolSetFinishCommand())
                .then(coolRemoveCommand());
        return builder.build();
    }

    public LiteralCommandNode<CommandSourceStack> coolJoinCommand() {
        return Commands.literal("join")
                .requires(ctx -> ctx.getSender().hasPermission("ceyzel.join"))
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage("Use /join <map>");
                    return 0;
                })
                .then(Commands.argument("map", StringArgumentType.string()).suggests((ctx, builder) -> {
                    plugin.getMaps().keySet()
                            .stream()
                            .filter(entry -> entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                            .forEach(builder::suggest);
                    return builder.buildFuture();
                }).executes(ctx -> {
                            if (ctx.getSource().getSender() instanceof Player player) {
                                ParkourMap map = plugin.getMap(ctx.getArgument("map", String.class));
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

    public LiteralCommandNode<CommandSourceStack> coolCreateCommand() {
        return Commands.literal("create")
                .then(Commands.argument("map", StringArgumentType.string()).executes((ctx) -> {
                    var mapName = ctx.getArgument("map", String.class);
                    if (plugin.getMaps().containsKey(mapName)) {
                        ctx.getSource().getSender().sendMessage("Карта с таким названием уже существует");
                        return 0;
                    }

                    ParkourMap newMap = new ParkourMap(mapName);
                    plugin.getMaps().put(mapName, newMap);
                    plugin.saveMap(newMap);
                    ctx.getSource().getSender().sendMessage(ChatColor.GREEN + "Карта '" + mapName + "' создана!");

                    return 1;
                })).build();
    }

    public LiteralCommandNode<CommandSourceStack> coolSetStartCommand() {
        return Commands.literal("setstart")
                .then(Commands.argument("map", StringArgumentType.string())
                        .suggests(this::mapsSuggestions)
                        .executes(ctx -> {
                            var mapname = ctx.getArgument("map", String.class);
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
                                return 0;
                            }

                            ParkourMap map = plugin.getMap(mapname);
                            if (map == null) {
                                player.sendMessage(ChatColor.RED + "Карта не найдена!");
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
                            player.sendMessage(ChatColor.GREEN + "Стартовая точка установлена!");

                            return 1;
                        }))
                .build();
    }

    public LiteralCommandNode<CommandSourceStack> coolSetCheckpointCommand() {
        return Commands.literal("setcheckpoint").then(
                Commands.argument("map", StringArgumentType.string())
                        .suggests(this::mapsSuggestions)
                        .executes(ctx -> {
                            var mapname = ctx.getArgument("map", String.class);
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
                                return 0;
                            }

                            ParkourMap map = plugin.getMap(mapname);
                            if (map == null) {
                                player.sendMessage(ChatColor.RED + "Карта не найдена!");
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
                        })
        ).build();
    }

    public LiteralCommandNode<CommandSourceStack> coolSetFinishCommand() {
        return Commands.literal("setfinish").then(
                Commands.argument("map", StringArgumentType.string())
                        .suggests(this::mapsSuggestions)
                        .executes(ctx -> {
                            var mapname = ctx.getArgument("map", String.class);
                            if (!(ctx.getSource().getSender() instanceof Player player)) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Только игроки могут использовать эту команду!");
                                return 0;
                            }

                            ParkourMap map = plugin.getMap(mapname);
                            if (map == null) {
                                player.sendMessage(ChatColor.RED + "Карта не найдена!");
                                return 0;
                            }

                            Block block = player.getLocation().getBlock();
                            block.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
                            map.setFinish(block);
                            plugin.saveMap(map);
                            player.sendMessage(ChatColor.GREEN + "Финишная точка установлена!");

                            return 1;
                        })
        ).build();
    }

    public LiteralCommandNode<CommandSourceStack> coolRemoveCommand() {
        return Commands.literal("remove")
                .then(Commands.argument("map", StringArgumentType.string())
                        .suggests(this::mapsSuggestions)
                        .executes(ctx -> {
                            var mapName = ctx.getArgument("map", String.class);
                            ParkourMap map = plugin.getMap(mapName);
                            if (map == null) {
                                ctx.getSource().getSender().sendMessage(ChatColor.RED + "Карта не найдена!");
                                return 0;
                            }

                            plugin.getMaps().remove(mapName);
                            plugin.getConfig().set(mapName, null);
                            plugin.saveConfig();
                            ctx.getSource().getSender().sendMessage(ChatColor.GREEN + "Карта '" + mapName + "' удалена!");

                            return 1;
                        }))
                .build();
    }
}