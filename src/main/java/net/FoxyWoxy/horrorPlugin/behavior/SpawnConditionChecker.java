package net.FoxyWoxy.horrorPlugin.behavior;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.manager.PlayerData;
import net.FoxyWoxy.horrorPlugin.util.GeometryUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.List;

public class SpawnConditionChecker {

    private final HorrorPlugin plugin;
    public SpawnConditionChecker(HorrorPlugin plugin) { this.plugin = plugin; }

    public boolean canSpawn(Player player, PlayerData data) {
        if (!plugin.getConfig().getBoolean("stalker.enabled", true)) return false;
        if (!player.isOnline()) return false;

        boolean allowCreative = plugin.getConfig().getBoolean("stalker.allow-creative", false);
        if (!allowCreative && (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR)) return false;

        if (player.hasPermission("horror.immune")) return false;
        if (data.hasActiveStalker()) return false;
        if (!data.isCooldownExpired()) return false;
        if (data.isForceImmune()) return false;

        List<String> worlds = plugin.getConfig().getStringList("stalker.allowed-worlds");
        if (!worlds.isEmpty() && !worlds.contains(player.getWorld().getName())) return false;

        int    avoidCount  = plugin.getConfig().getInt("stalker.player-avoid-count", 1);
        double avoidRadius = plugin.getConfig().getDouble("stalker.player-avoid-radius", 80.0);
        if (GeometryUtil.countNearbyPlayers(player.getLocation(), avoidRadius,
                plugin.getServer().getOnlinePlayers(), player) >= avoidCount) return false;

        if (player.isDead() || player.isSleeping()) return false;
        return true;
    }

    public boolean shouldDespawn(Player player, PlayerData data) {
        if (!data.hasActiveStalker()) return false;
        var loc = data.getActiveStalker().getLocation();

        if (loc != null && !loc.getWorld().equals(player.getWorld())) return true;

        double prox = plugin.getConfig().getDouble("stalker.proximity-despawn-distance", 10.0);
        if (loc != null && GeometryUtil.horizontalDistance(player.getLocation(), loc) < prox) return true;

        long gazeMs = plugin.getConfig().getLong("stalker.gaze-hold-ms", 400L);
        if (data.isGazingAtStalker() && data.gazeElapsedMs() >= gazeMs) return true;

        double avoidRadius = plugin.getConfig().getDouble("stalker.player-avoid-radius", 80.0);
        if (GeometryUtil.countNearbyPlayers(player.getLocation(), avoidRadius,
                plugin.getServer().getOnlinePlayers(), player)
                >= plugin.getConfig().getInt("stalker.player-avoid-count", 1)) return true;

        long lingerMin = plugin.getConfig().getLong("stalker.linger-min", 6L) * 1000L;
        long lingerMax = plugin.getConfig().getLong("stalker.linger-max", 18L) * 1000L;
        long expires   = data.getActiveStalker().getSpawnTime()
                + lingerMin + (long)(Math.random() * (lingerMax - lingerMin));
        if (System.currentTimeMillis() > expires) return true;

        if (player.isDead()) return true;
        boolean allowCreative = plugin.getConfig().getBoolean("stalker.allow-creative", false);
        if (!allowCreative && (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR)) return true;

        return false;
    }
}