package net.FoxyWoxy.horrorPlugin.entity;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;

public class StalkerEntity {

    private final HorrorPlugin plugin;
    private final Player       target;
    private       ItemDisplay  display;
    private       PoseType     currentPose;
    private       long         spawnTime;
    private       boolean      alive;

    public StalkerEntity(HorrorPlugin plugin, Player target) {
        this.plugin = plugin;
        this.target = target;
        this.alive  = false;
    }

    public void spawn(Location location, PoseType pose) {
        this.currentPose = pose;
        this.spawnTime   = System.currentTimeMillis();
        this.alive       = true;

        display = location.getWorld().spawn(location, ItemDisplay.class, entity -> {
            entity.setItemStack(buildItem(pose));
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setViewRange(1.0f);
        });

        hideFromAll();

        if (plugin.isDebug())
            plugin.getLogger().info("[Stalker] Spawned for " + target.getName() + " pose=" + pose.name());
    }

    public void setPose(PoseType pose) {
        if (!alive || display == null) return;
        this.currentPose = pose;
        display.setItemStack(buildItem(pose));
    }

    public void despawn() {
        if (!alive) return;
        alive = false;
        if (display != null && !display.isDead()) {
            try {
                var user = PacketEvents.getAPI().getPlayerManager().getUser(target);
                if (user != null)
                    user.sendPacket(new WrapperPlayServerDestroyEntities(display.getEntityId()));
            } catch (Exception e) {
                plugin.getLogger().warning("[Stalker] Destroy packet failed: " + e.getMessage());
            }
            display.remove();
            display = null;
        }
    }

    private ItemStack buildItem(PoseType pose) {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta  meta = item.getItemMeta();
        meta.setItemModel(new NamespacedKey("horrorplugin", pose.getItemModelName()));
        item.setItemMeta(meta);
        return item;
    }

    private void hideFromAll() {
        if (display == null) return;
        int eid = display.getEntityId();

        // immediate destroy packet to all non-targets
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.equals(target)) continue;
            sendDestroyPacket(online, eid);
        }

        // delayed second pass to catch any timing issues
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (display == null || display.isDead()) return;
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (online.equals(target)) continue;
                sendDestroyPacket(online, eid);
            }
        }, 2L);
    }

    private void sendDestroyPacket(Player player, int entityId) {
        try {
            var user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            if (user != null)
                user.sendPacket(new WrapperPlayServerDestroyEntities(entityId));
        } catch (Exception ignored) {}
    }

    public void hideFromPlayer(Player player) {
        if (display == null || display.isDead()) return;
        if (player.equals(target)) return;
        sendDestroyPacket(player, display.getEntityId());
    }

    private void facePlayer(Location stalkerLoc) {
        if (display == null) return;

        Location playerLoc = target.getEyeLocation();

        double dx = playerLoc.getX() - stalkerLoc.getX();
        double dz = playerLoc.getZ() - stalkerLoc.getZ();

        // Angle from stalker to player — model face is NORTH (-Z) so we offset by 180
        float yawDeg = (float) Math.toDegrees(Math.atan2(-dx, dz)) + 180f;
        float yawRad = (float) Math.toRadians(yawDeg);

        // Build a Y-axis rotation quaternion
        float sin = (float) Math.sin(yawRad / 2f);
        float cos = (float) Math.cos(yawRad / 2f);

        org.bukkit.util.Transformation transformation = new org.bukkit.util.Transformation(
                new org.joml.Vector3f(0, 0, 0),           // translation
                new org.joml.Quaternionf(0, sin, 0, cos), // left rotation (Y axis)
                new org.joml.Vector3f(1, 1, 1),           // scale
                new org.joml.Quaternionf(0, 0, 0, 1)      // right rotation (none)
        );

        display.setTransformation(transformation);
    }

    public boolean     isAlive()        { return alive && display != null && !display.isDead(); }
    public Player      getTarget()      { return target; }
    public ItemDisplay getDisplay()     { return display; }
    public PoseType    getCurrentPose() { return currentPose; }
    public long        getSpawnTime()   { return spawnTime; }

    public Location getLocation() {
        return (display != null && !display.isDead()) ? display.getLocation() : null;
    }
}