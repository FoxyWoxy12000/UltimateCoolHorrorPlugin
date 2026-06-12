package net.FoxyWoxy.horrorPlugin.commands;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.entity.PoseType;
import net.FoxyWoxy.horrorPlugin.entity.StalkerEntity;
import net.FoxyWoxy.horrorPlugin.manager.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HorrorCommand implements CommandExecutor, TabCompleter {

    private final HorrorPlugin plugin;

    public HorrorCommand(HorrorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("horror.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            // /foxyshorrorplugin spawn [player]
            // Force spawns the stalker on a player
            case "spawn" -> {
                Player target = resolveTarget(sender, args, 1);
                if (target == null) return true;
                plugin.getStalkerManager().forceSpawn(target);
                sender.sendMessage("§aStalker force-spawned on §f" + target.getName());
            }

            // /foxyshorrorplugin despawn [player]
            // Removes the stalker from a player
            case "despawn" -> {
                Player target = resolveTarget(sender, args, 1);
                if (target == null) return true;
                PlayerData data = plugin.getPlayerDataManager().get(target);
                if (data.hasActiveStalker()) {
                    data.clearActiveStalker();
                    sender.sendMessage("§aStalker despawned for §f" + target.getName());
                } else {
                    sender.sendMessage("§e" + target.getName() + " has no active stalker.");
                }
            }

            // /foxyshorrorplugin despawnall
            // Removes all stalkers from all players
            case "despawnall" -> {
                plugin.getStalkerManager().despawnAll();
                sender.sendMessage("§aAll stalkers despawned.");
            }

            // /foxyshorrorplugin freeze [player]
            // Stalker will not despawn when looked at (toggle)
            case "freeze" -> {
                Player target = resolveTarget(sender, args, 1);
                if (target == null) return true;
                PlayerData data = plugin.getPlayerDataManager().get(target);
                data.setGazeLocked(!data.isGazeLocked());
                sender.sendMessage("§eGaze-lock for §f" + target.getName()
                        + "§e: " + (data.isGazeLocked() ? "§aON (won't despawn on look)" : "§cOFF"));
            }

            // /foxyshorrorplugin nocooldown [player]
            // Resets spawn cooldown to 0 so stalker can spawn immediately
            case "nocooldown" -> {
                Player target = resolveTarget(sender, args, 1);
                if (target == null) return true;
                plugin.getPlayerDataManager().get(target).setCooldown(0);
                sender.sendMessage("§aCooldown cleared for §f" + target.getName());
            }

            // /foxyshorrorplugin pose <pose> [player]
            // Changes the active stalker's pose
            case "pose" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /foxyshorrorplugin pose <pose> [player]");
                    sender.sendMessage("§7Poses: " + Arrays.stream(PoseType.values())
                            .map(p -> p.name().toLowerCase())
                            .collect(Collectors.joining(", ")));
                    return true;
                }
                PoseType pose;
                try {
                    pose = PoseType.valueOf(args[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cUnknown pose. Options: " + Arrays.stream(PoseType.values())
                            .map(p -> p.name().toLowerCase())
                            .collect(Collectors.joining(", ")));
                    return true;
                }
                Player target = resolveTarget(sender, args, 2);
                if (target == null) return true;
                PlayerData data = plugin.getPlayerDataManager().get(target);
                if (!data.hasActiveStalker()) {
                    sender.sendMessage("§c" + target.getName() + " has no active stalker. Spawn one first.");
                    return true;
                }
                data.getActiveStalker().setPose(pose);
                sender.sendMessage("§aPose set to §f" + pose.name() + "§a for §f" + target.getName());
            }

            // /foxyshorrorplugin info [player]
            // Shows current stalker state for a player
            case "info" -> {
                Player target = resolveTarget(sender, args, 1);
                if (target == null) return true;
                PlayerData data = plugin.getPlayerDataManager().get(target);
                sender.sendMessage("§6--- Stalker Info: " + target.getName() + " ---");
                sender.sendMessage("§eActive stalker: §f" + data.hasActiveStalker());
                if (data.hasActiveStalker()) {
                    StalkerEntity s = data.getActiveStalker();
                    sender.sendMessage("§ePose: §f" + s.getCurrentPose().name());
                    sender.sendMessage("§eLocation: §f" + formatLoc(s.getLocation()));
                    sender.sendMessage("§eAlive ms: §f" + (System.currentTimeMillis() - s.getSpawnTime()) + "ms");
                }
                sender.sendMessage("§eCooldown remaining: §f"
                        + Math.max(0, (data.getNextSpawnAllowedAt() - System.currentTimeMillis()) / 1000L) + "s");
                sender.sendMessage("§eGaze locked: §f" + data.isGazeLocked());
                sender.sendMessage("§eParanoia active: §f" + data.isParanoiaActive());
                if (data.isParanoiaActive())
                    sender.sendMessage("§eParanoia remaining: §f" + data.paranoiaRemainingSeconds() + "s");
            }

            // /foxyshorrorplugin paranoia [player]
            // Applies paranoia effect to a player immediately
            case "paranoia" -> {
                Player target = resolveTarget(sender, args, 1);
                if (target == null) return true;
                long duration = plugin.getConfig().getLong("paranoia-potion.duration", 120L);
                plugin.getPlayerDataManager().get(target).applyParanoia(duration);
                sender.sendMessage("§aParanoia applied to §f" + target.getName()
                        + "§a for §f" + duration + "s");
            }

            // /foxyshorrorplugin immune [player]
            // Toggles horror.immune permission workaround for testing
            case "immune" -> {
                Player target = resolveTarget(sender, args, 1);
                if (target == null) return true;
                PlayerData data = plugin.getPlayerDataManager().get(target);
                data.setForceImmune(!data.isForceImmune());
                sender.sendMessage("§eImmunity for §f" + target.getName()
                        + "§e: " + (data.isForceImmune() ? "§aON" : "§cOFF"));
            }

            // /foxyshorrorplugin reload
            // Reloads config
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getStalkerManager().reloadConfig();
                sender.sendMessage("§aConfig reloaded.");
            }

            // /foxyshorrorplugin debug
            // Toggles debug logging
            case "debug" -> {
                boolean cur = plugin.getConfig().getBoolean("debug.enabled", false);
                plugin.getConfig().set("debug.enabled", !cur);
                sender.sendMessage("§eDebug mode: " + (!cur ? "§aON" : "§cOFF"));
            }

            // /foxyshorrorplugin giveeye [player]
            // Gives the cursed eye item
            case "giveeye" -> {
                Player target = resolveTarget(sender, args, 1);
                if (target == null) return true;
                target.getInventory().addItem(plugin.getCursedEyeItem().createItem());
                sender.sendMessage("§aGave Cursed Eye to §f" + target.getName());
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6--- Horror Plugin Commands ---");
        sender.sendMessage("§e/foxyshorrorplugin spawn [player]       §7- Force spawn stalker");
        sender.sendMessage("§e/foxyshorrorplugin despawn [player]     §7- Remove stalker");
        sender.sendMessage("§e/foxyshorrorplugin despawnall           §7- Remove all stalkers");
        sender.sendMessage("§e/foxyshorrorplugin freeze [player]      §7- Toggle gaze-lock (won't despawn on look)");
        sender.sendMessage("§e/foxyshorrorplugin nocooldown [player]  §7- Clear spawn cooldown");
        sender.sendMessage("§e/foxyshorrorplugin pose <pose> [player] §7- Set active stalker pose");
        sender.sendMessage("§e/foxyshorrorplugin info [player]        §7- Show stalker state");
        sender.sendMessage("§e/foxyshorrorplugin paranoia [player]    §7- Apply paranoia effect");
        sender.sendMessage("§e/foxyshorrorplugin immune [player]      §7- Toggle immunity");
        sender.sendMessage("§e/foxyshorrorplugin giveeye [player]     §7- Give cursed eye item");
        sender.sendMessage("§e/foxyshorrorplugin reload               §7- Reload config");
        sender.sendMessage("§e/foxyshorrorplugin debug                §7- Toggle debug logging");
    }

    private Player resolveTarget(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            Player p = plugin.getServer().getPlayer(args[index]);
            if (p == null) { sender.sendMessage("§cPlayer not found: " + args[index]); return null; }
            return p;
        }
        if (sender instanceof Player p) return p;
        sender.sendMessage("§cSpecify a player name.");
        return null;
    }

    private String formatLoc(org.bukkit.Location loc) {
        if (loc == null) return "null";
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("spawn","despawn","despawnall","freeze","nocooldown",
                            "pose","info","paranoia","immune","giveeye","reload","debug")
                    .stream().filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("pose")) {
            return Arrays.stream(PoseType.values())
                    .map(p -> p.name().toLowerCase())
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 || (args.length == 3 && args[0].equalsIgnoreCase("pose"))) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}