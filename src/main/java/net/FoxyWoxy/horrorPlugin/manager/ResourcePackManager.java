package net.FoxyWoxy.horrorPlugin.manager;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import org.bukkit.entity.Player;

public class ResourcePackManager {

    private final HorrorPlugin plugin;
    public ResourcePackManager(HorrorPlugin plugin) { this.plugin = plugin; }

    public void sendPack(Player player) {
        String  url      = plugin.getConfig().getString("resource-pack.url", "");
        String  sha1     = plugin.getConfig().getString("resource-pack.sha1", "");
        boolean required = plugin.getConfig().getBoolean("resource-pack.required", true);
        String  prompt   = plugin.getConfig().getString("resource-pack.prompt", "Horror resource pack required.");

        if (url == null || url.isBlank()) return;
        try {
            player.setResourcePack(url, sha1.isBlank() ? null : sha1, required, prompt);
        } catch (Exception e) {
            plugin.getLogger().warning("[ResourcePack] Failed for " + player.getName() + ": " + e.getMessage());
        }
    }
}