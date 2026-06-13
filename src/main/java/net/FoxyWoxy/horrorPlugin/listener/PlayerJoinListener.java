package net.FoxyWoxy.horrorPlugin.listener;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.manager.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final HorrorPlugin plugin;
    public PlayerJoinListener(HorrorPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();

        // send resource pack after 1 second
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (joined.isOnline())
                plugin.getResourcePackManager().sendPack(joined);
        }, 20L);

        // hide all existing stalkers from the joining player after they're fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!joined.isOnline()) return;
            for (PlayerData data : plugin.getPlayerDataManager().all()) {
                if (!data.hasActiveStalker()) continue;
                data.getActiveStalker().hideFromPlayer(joined);
            }
        }, 5L);
    }
}