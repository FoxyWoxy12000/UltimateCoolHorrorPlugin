package net.FoxyWoxy.horrorPlugin.manager;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.behavior.SpawnConditionChecker;
import net.FoxyWoxy.horrorPlugin.behavior.StalkerAI;
import net.FoxyWoxy.horrorPlugin.entity.PoseType;
import net.FoxyWoxy.horrorPlugin.entity.StalkerEntity;
import net.FoxyWoxy.horrorPlugin.util.GeometryUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class StalkerManager {

    private final HorrorPlugin          plugin;
    private final StalkerAI             ai;
    private final SpawnConditionChecker checker;

    private long   cooldownMin, cooldownMax;
    private double gazeAngle;

    public StalkerManager(HorrorPlugin plugin) {
        this.plugin  = plugin;
        this.ai      = new StalkerAI(plugin);
        this.checker = new SpawnConditionChecker(plugin);
        reloadConfig();
    }

    public void tick() {
        for (PlayerData data : plugin.getPlayerDataManager().all()) {
            Player p = data.getPlayer();
            if (!p.isOnline()) continue;
            if (data.hasActiveStalker()) tickActiveStalker(p, data);
            else                         tickSpawnCheck(p, data);
        }
    }

    private void tickActiveStalker(Player player, PlayerData data) {
        StalkerEntity stalker = data.getActiveStalker();
        if (!stalker.isAlive()) {
            data.clearActiveStalker();
            applyRandomCooldown(data);

            return;
        }

        stalker.updateFacing();

        Location loc = stalker.getLocation();
        if (loc != null) {
            double angle = GeometryUtil.angleTo(player, loc);
            if (angle <= gazeAngle) data.startGaze(); else data.clearGaze();
            if (GeometryUtil.horizontalDistance(player.getLocation(), loc) < 15.0)
                ai.playProximitySound(player);
        }

        if (!data.isGazeLocked() && checker.shouldDespawn(player, data)) despawnStalker(player, data);
    }

    private void tickSpawnCheck(Player player, PlayerData data) {
        if (!checker.canSpawn(player, data)) return;
        Location loc = ai.findSpawnLocation(player);
        if (loc == null) return;
        PoseType      pose    = ai.choosePose(loc);
        StalkerEntity stalker = new StalkerEntity(plugin, player);
        stalker.spawn(loc, pose);
        data.setActiveStalker(stalker);
        ai.playAmbientSound(player);
    }

    private void despawnStalker(Player player, PlayerData data) {
        data.clearActiveStalker();
        data.clearGaze();
        applyRandomCooldown(data);
    }

    private void applyRandomCooldown(PlayerData data) {
        long base = cooldownMin + ThreadLocalRandom.current().nextLong(cooldownMax - cooldownMin);
        if (data.isParanoiaActive())
            base /= (long) plugin.getConfig().getDouble("paranoia-potion.spawn-rate-multiplier", 2.0);
        data.setCooldown(Math.max(1L, base));
    }

    public void forceSpawn(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data.hasActiveStalker()) data.clearActiveStalker();
        data.setCooldown(0);
        tickSpawnCheck(player, data);
    }

    public void despawnAll() {
        for (PlayerData data : plugin.getPlayerDataManager().all())
            if (data.hasActiveStalker()) data.clearActiveStalker();
    }

    public void reloadConfig() {
        cooldownMin = plugin.getConfig().getLong("stalker.cooldown-min", 15L);
        cooldownMax = plugin.getConfig().getLong("stalker.cooldown-max", 45L);
        gazeAngle   = plugin.getConfig().getDouble("stalker.gaze-despawn-angle", 8.0);
    }
}