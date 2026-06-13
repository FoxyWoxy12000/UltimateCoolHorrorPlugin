package net.FoxyWoxy.horrorPlugin.listener;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.manager.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerJoinListener implements Listener {

    private final HorrorPlugin plugin;
    public PlayerJoinListener(HorrorPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();

        // send resource pack after 2 seconds then mark as pending
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!joined.isOnline()) return;
            plugin.getResourcePackManager().sendPack(joined);
            // mark as pending AFTER sending so spurious early events are ignored
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                            plugin.getResourcePackListener().addPending(joined.getUniqueId())
                    , 5L);
        }, 40L);

        // hide stalkers from joining player
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!joined.isOnline()) return;
            for (PlayerData data : plugin.getPlayerDataManager().all()) {
                if (!data.hasActiveStalker()) continue;
                data.getActiveStalker().hideFromPlayer(joined);
            }
        }, 5L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player respawned = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!respawned.isOnline()) return;
            for (PlayerData data : plugin.getPlayerDataManager().all()) {
                if (!data.hasActiveStalker()) continue;
                data.getActiveStalker().hideFromPlayer(respawned);
            }
        }, 10L);
    }
}