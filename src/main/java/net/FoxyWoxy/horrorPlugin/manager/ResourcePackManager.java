package net.FoxyWoxy.horrorPlugin.manager;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import org.bukkit.entity.Player;

public class ResourcePackManager {

    private final HorrorPlugin plugin;
    public ResourcePackManager(HorrorPlugin plugin) { this.plugin = plugin; }

    public void sendPack(Player player) {
        String url    = plugin.getConfig().getString("resource-pack.url", "");
        String sha1   = plugin.getConfig().getString("resource-pack.sha1", "");
        String prompt = plugin.getConfig().getString("resource-pack.prompt", "Horror resource pack required.");
        boolean required = plugin.getConfig().getBoolean("resource-pack.required", true);

        if (url == null || url.isBlank()) return;

        try {
            player.sendResourcePacks(
                    net.kyori.adventure.resource.ResourcePackRequest.resourcePackRequest()
                            .packs(
                                    net.kyori.adventure.resource.ResourcePackInfo.resourcePackInfo(
                                            java.util.UUID.nameUUIDFromBytes(url.getBytes()),
                                            java.net.URI.create(url),
                                            sha1.isBlank() ? "" : sha1
                                    )
                            )
                            .prompt(net.kyori.adventure.text.Component.text(prompt))
                            .required(false)
                            .build()
            );
        } catch (Exception e) {
            plugin.getLogger().warning("[ResourcePack] Failed for " + player.getName() + ": " + e.getMessage());
        }
    }
}