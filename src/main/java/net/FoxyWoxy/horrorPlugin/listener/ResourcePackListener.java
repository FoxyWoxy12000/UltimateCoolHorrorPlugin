package net.FoxyWoxy.horrorPlugin.listener;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcePackListener implements Listener {

    private final HorrorPlugin plugin;
    private final Set<UUID> loadedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public ResourcePackListener(HorrorPlugin plugin) { this.plugin = plugin; }

    public void removePlayer(UUID uuid) { loadedPlayers.remove(uuid); }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED -> {
                loadedPlayers.add(uuid);
                if (plugin.isDebug())
                    plugin.getLogger().info("[ResourcePack] " + event.getPlayer().getName() + " loaded OK.");
            }
            case DECLINED -> {
                // Only kick if config says required AND they haven't already loaded it
                if (!plugin.getConfig().getBoolean("resource-pack.required", true)) return;
                if (loadedPlayers.contains(uuid)) return;
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (event.getPlayer().isOnline() && !loadedPlayers.contains(uuid))
                        event.getPlayer().kick(
                                Component.text("You must accept the resource pack to play on this server.")
                                        .color(NamedTextColor.RED)
                        );
                }, 20L);
            }
            // ACCEPTED, DOWNLOADED, FAILED_DOWNLOAD, DISCARDED, etc. → do nothing
            // We only care about explicit decline or confirmed success
            default -> {}
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        loadedPlayers.remove(event.getPlayer().getUniqueId());
    }
}