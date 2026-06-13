package net.FoxyWoxy.horrorPlugin.listener;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackListener implements Listener {

    private final HorrorPlugin plugin;

    public ResourcePackListener(HorrorPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (!plugin.getConfig().getBoolean("resource-pack.required", true)) return;

        switch (event.getStatus()) {
            case DECLINED -> {
                // player explicitly said no — kick after short delay
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (event.getPlayer().isOnline())
                        event.getPlayer().kick(
                                Component.text("You must accept the resource pack to play on this server.")
                                        .color(NamedTextColor.RED)
                        );
                }, 40L); // 2 second delay
            }
            case FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD, DISCARDED -> {
                // download failed — give them a moment then kick
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (event.getPlayer().isOnline())
                        event.getPlayer().kick(
                                Component.text("Resource pack failed to download. Please try rejoining.")
                                        .color(NamedTextColor.RED)
                        );
                }, 60L); // 3 second delay
            }
            case SUCCESSFULLY_LOADED -> {
                if (plugin.isDebug())
                    plugin.getLogger().info("[ResourcePack] " + event.getPlayer().getName() + " loaded the pack successfully.");
            }
            // ACCEPTED and DOWNLOADED mean they are in the process of downloading — do nothing
            default -> {}
        }
    }
}