package net.FoxyWoxy.horrorPlugin.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class GeometryUtil {

    private GeometryUtil() {}

    public static double angleTo(Player player, Location target) {
        Vector look     = player.getLocation().getDirection().normalize();
        Vector toTarget = target.clone().subtract(player.getEyeLocation()).toVector().normalize();
        double dot      = Math.max(-1.0, Math.min(1.0, look.dot(toTarget)));
        return Math.toDegrees(Math.acos(dot));
    }

    public static boolean hasLineOfSight(Player player, Location target) {
        Vector eye       = player.getEyeLocation().toVector();
        Vector dest      = target.toVector();
        Vector direction = dest.clone().subtract(eye).normalize();
        double distance  = eye.distance(dest);

        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(), direction, distance,
                org.bukkit.FluidCollisionMode.NEVER, true);
        return result == null;
    }

    public static Location findFovEdgeLocation(Player player, double minDist, double maxDist,
                                               double fovEdgeAngle, double variance, int attempts) {
        Location eye   = player.getEyeLocation();
        float    yaw   = eye.getYaw();
        World    world = player.getWorld();

        for (int i = 0; i < attempts; i++) {
            double side         = Math.random() < 0.5 ? 1 : -1;
            double angle        = fovEdgeAngle + (Math.random() * variance * 2 - variance);
            double totalYawRad  = Math.toRadians(yaw + side * angle);
            double dist         = minDist + Math.random() * (maxDist - minDist);

            double dx = -Math.sin(totalYawRad) * dist;
            double dz =  Math.cos(totalYawRad) * dist;

            Location candidate = eye.clone().add(dx, 0, dz);
            candidate.setY(findSolidY(world, candidate, (int) eye.getY()));

            if (candidate.getBlock().getType().isAir()
                    && candidate.clone().add(0, 1, 0).getBlock().getType().isAir())
                return candidate;
        }
        return null;
    }

    private static double findSolidY(World world, Location base, int startY) {
        for (int y = startY; y > world.getMinHeight(); y--) {
            Block b = world.getBlockAt(base.getBlockX(), y, base.getBlockZ());
            if (b.getType().isSolid()) return y + 1.0;
        }
        return startY;
    }

    public static double horizontalDistance(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static int countNearbyPlayers(Location location, double radius,
                                         Iterable<? extends Player> players, Player exclude) {
        int count = 0;
        for (Player p : players) {
            if (p.equals(exclude)) continue;
            if (!p.getWorld().equals(location.getWorld())) continue;
            if (horizontalDistance(p.getLocation(), location) <= radius) count++;
        }
        return count;
    }
}