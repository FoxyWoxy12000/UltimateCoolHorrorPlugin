package net.FoxyWoxy.horrorPlugin.behavior;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.entity.PoseType;
import net.FoxyWoxy.horrorPlugin.util.GeometryUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Random;

public class StalkerAI {

    private final HorrorPlugin plugin;
    private final Random       rng = new Random();

    public StalkerAI(HorrorPlugin plugin) { this.plugin = plugin; }

    public Location findSpawnLocation(Player player) {
        double minDist = plugin.getConfig().getDouble("stalker.spawn-distance-min", 18.0);
        double maxDist = plugin.getConfig().getDouble("stalker.spawn-distance-max", 48.0);

        Location loc = GeometryUtil.findFovEdgeLocation(player, minDist, maxDist, 62.0, 12.0, 20);
        if (loc != null && GeometryUtil.hasLineOfSight(player, loc)) return loc;

        for (int i = 0; i < 15; i++) {
            double angle = rng.nextDouble() * 360.0;
            double dist  = minDist + rng.nextDouble() * (maxDist - minDist);
            double dx    = Math.cos(Math.toRadians(angle)) * dist;
            double dz    = Math.sin(Math.toRadians(angle)) * dist;

            Location candidate = player.getLocation().clone().add(dx, 0, dz);
            candidate.setY(candidate.getWorld().getHighestBlockYAt(candidate) + 1.0);

            if (isValidSpawnBlock(candidate) && GeometryUtil.hasLineOfSight(player, candidate))
                return candidate;
        }
        return null;
    }

    public PoseType choosePose(Location loc) {
        if (loc == null) return PoseType.STANDING;

        Block above1 = loc.clone().add(0, 1, 0).getBlock();
        Block above2 = loc.clone().add(0, 2, 0).getBlock();

        if (above1.getType().isSolid() || above1.getType().name().endsWith("_SLAB"))
            return PoseType.CROUCHING;

        if (above2.getType() == Material.OAK_FENCE
                || above2.getType() == Material.DARK_OAK_FENCE
                || above2.getType() == Material.NETHER_BRICK_FENCE
                || above2.getType() == Material.IRON_BARS)
            return PoseType.HANGING;

        if (hasAdjacentWall(loc))
            return rng.nextBoolean() ? PoseType.PEEKING : PoseType.STANDING;

        PoseType[] openPoses = { PoseType.STANDING, PoseType.TILTED, PoseType.REACHING, PoseType.SITTING };
        return openPoses[rng.nextInt(openPoses.length)];
    }

    public void playAmbientSound(Player player) {
        if (!plugin.getConfig().getBoolean("sounds.ambient-enabled", true)) return;
        float vol = (float) plugin.getConfig().getDouble("sounds.ambient-volume", 0.4);
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, vol, 0.5f);
    }

    public void playProximitySound(Player player) {
        if (!plugin.getConfig().getBoolean("sounds.ambient-enabled", true)) return;
        float vol = (float) plugin.getConfig().getDouble("sounds.proximity-volume", 0.7);
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, vol, 0.3f);
    }

    private boolean isValidSpawnBlock(Location loc) {
        return loc.getBlock().getType().isAir()
                && loc.clone().add(0, 1, 0).getBlock().getType().isAir();
    }

    private boolean hasAdjacentWall(Location loc) {
        int[][] offsets = { {1,0},{-1,0},{0,1},{0,-1} };
        for (int[] o : offsets)
            if (loc.clone().add(o[0], 0, o[1]).getBlock().getType().isSolid()) return true;
        return false;
    }
}