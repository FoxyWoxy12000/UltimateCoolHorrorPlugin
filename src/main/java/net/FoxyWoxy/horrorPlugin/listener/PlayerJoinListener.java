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

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (joined.isOnline())
                plugin.getResourcePackManager().sendPack(joined);
        }, 20L);

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

        // delay needed — entity tracker resends all entities after respawn
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!respawned.isOnline()) return;
            for (PlayerData data : plugin.getPlayerDataManager().all()) {
                if (!data.hasActiveStalker()) continue;
                data.getActiveStalker().hideFromPlayer(respawned);
            }
        }, 10L);  // slightly longer delay than join to account for respawn sequence
    }
}