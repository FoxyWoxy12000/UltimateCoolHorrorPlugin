package net.FoxyWoxy.horrorPlugin.behavior;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.entity.PoseType;
import net.FoxyWoxy.horrorPlugin.util.GeometryUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StalkerAI {

    private final HorrorPlugin plugin;
    private final Random rng = new Random();

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

        List<PoseType> valid = new ArrayList<>();

        Block at     = loc.getBlock();
        Block above1 = loc.clone().add(0, 1, 0).getBlock();
        Block above2 = loc.clone().add(0, 2, 0).getBlock();
        Block below  = loc.clone().add(0, -1, 0).getBlock();

        boolean hasNorthWall = loc.clone().add(0, 0, -1).getBlock().getType().isSolid();
        boolean hasSouthWall = loc.clone().add(0, 0,  1).getBlock().getType().isSolid();
        boolean hasEastWall  = loc.clone().add(1, 0,  0).getBlock().getType().isSolid();
        boolean hasWestWall  = loc.clone().add(-1, 0, 0).getBlock().getType().isSolid();
        boolean hasAnyWall   = hasNorthWall || hasSouthWall || hasEastWall || hasWestWall;

        boolean lowCeiling   = above1.getType().isSolid() || isSlab(above1);
        boolean slab         = isSlab(above1) && !above1.getType().isSolid();
        boolean hasCeiling   = above2.getType().isSolid();
        boolean isFenceAbove = isFenceOrBars(above2);

        // ── Ceiling / hanging poses ──────────────────────────────
        if (isFenceAbove) {
            valid.add(PoseType.HANGING);
            valid.add(PoseType.HANGING_INVERTED);
        }
        if (hasCeiling) {
            valid.add(PoseType.HANGING_INVERTED);
            valid.add(PoseType.CLINGING_WALL);
        }

        // ── Slab poses ───────────────────────────────────────────
        if (lowCeiling) {
            valid.add(PoseType.UNDER_SLAB);
            valid.add(PoseType.CRAWLING);
        }
        if (isSlab(below) || isSlab(at)) {
            valid.add(PoseType.OVER_SLAB);
        }

        // ── Wall / corner poses ──────────────────────────────────
        if (hasAnyWall) {
            valid.add(PoseType.PEEK_LEFT);
            valid.add(PoseType.PEEK_RIGHT);
            valid.add(PoseType.PRESSED_WALL);
        }

        // ── Doorframe ────────────────────────────────────────────
        if (isDoorframe(loc)) {
            valid.add(PoseType.DOORFRAME);
        }

        // ── Crouching in tight spaces ────────────────────────────
        if (lowCeiling && hasAnyWall) {
            valid.add(PoseType.CROUCHING);
        }

        // ── Open area fallbacks ──────────────────────────────────
        if (valid.isEmpty()) {
            valid.add(PoseType.STANDING);
            valid.add(PoseType.TILTED);
            valid.add(PoseType.REACHING);
            valid.add(PoseType.SITTING);
        }

        return valid.get(rng.nextInt(valid.size()));
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

    // ── Helpers ──────────────────────────────────────────────────

    private boolean isValidSpawnBlock(Location loc) {
        return loc.getBlock().getType().isAir()
                && loc.clone().add(0, 1, 0).getBlock().getType().isAir();
    }

    private boolean isSlab(Block block) {
        return block.getType().name().endsWith("_SLAB");
    }

    private boolean isFenceOrBars(Block block) {
        String n = block.getType().name();
        return n.endsWith("_FENCE") || n.equals("IRON_BARS") || n.equals("NETHER_BRICK_FENCE");
    }

    private boolean isDoorframe(Location loc) {
        // A doorframe is a 2-block tall air gap with solid blocks on both sides
        Block here  = loc.getBlock();
        Block above = loc.clone().add(0, 1, 0).getBlock();
        if (!here.getType().isAir() || !above.getType().isAir()) return false;

        int solidSides = 0;
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            if (here.getRelative(face).getType().isSolid()) solidSides++;
        }
        return solidSides >= 2;
    }
}