package net.FoxyWoxy.horrorPlugin.listener;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.entity.StalkerEntity;
import net.FoxyWoxy.horrorPlugin.manager.PlayerData;
import net.FoxyWoxy.horrorPlugin.util.GeometryUtil;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerLookListener implements Listener {

    private final HorrorPlugin plugin;
    public PlayerLookListener(HorrorPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getPitch() == event.getTo().getPitch()
                && event.getFrom().getYaw() == event.getTo().getYaw()) return;

        PlayerData data = plugin.getPlayerDataManager().get(event.getPlayer());
        if (!data.hasActiveStalker()) { data.clearGaze(); return; }

        StalkerEntity stalker    = data.getActiveStalker();
        Location      stalkerLoc = stalker.getLocation();
        if (stalkerLoc == null) return;

        double gazeThreshold = plugin.getConfig().getDouble("stalker.gaze-despawn-angle", 8.0);
        double angle         = GeometryUtil.angleTo(event.getPlayer(), stalkerLoc);

        if (angle <= gazeThreshold) data.startGaze(); else data.clearGaze();
    }
}