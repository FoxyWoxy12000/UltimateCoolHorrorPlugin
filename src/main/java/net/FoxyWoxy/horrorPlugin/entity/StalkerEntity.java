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
            entity.setViewRange(0.0f);
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
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.equals(target)) continue;
            try {
                var user = PacketEvents.getAPI().getPlayerManager().getUser(online);
                if (user != null)
                    user.sendPacket(new WrapperPlayServerDestroyEntities(eid));
            } catch (Exception ignored) {}
        }
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