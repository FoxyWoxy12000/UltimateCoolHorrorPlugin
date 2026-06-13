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
        switch (event.getStatus()) {
            case DECLINED, FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD, DISCARDED -> {
                if (plugin.getConfig().getBoolean("resource-pack.required", true)) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            event.getPlayer().kick(
                                    Component.text("You must accept the resource pack to play on this server.")
                                            .color(NamedTextColor.RED)
                            )
                    );
                }
            }
            case SUCCESSFULLY_LOADED -> {
                if (plugin.isDebug())
                    plugin.getLogger().info("[ResourcePack] " + event.getPlayer().getName() + " loaded the pack successfully.");
            }
            default -> {}
        }
    }
}