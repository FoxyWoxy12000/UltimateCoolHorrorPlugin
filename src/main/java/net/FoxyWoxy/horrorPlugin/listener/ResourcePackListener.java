package net.FoxyWoxy.horrorPlugin.listener;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ResourcePackListener implements Listener {

    private final HorrorPlugin plugin;

    // players who have been sent the pack but haven't responded yet
    private final Set<UUID> pendingPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // players who have successfully loaded the pack
    private final Set<UUID> loadedPlayers  = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public ResourcePackListener(HorrorPlugin plugin) { this.plugin = plugin; }

    public void addPending(UUID uuid) {
        pendingPlayers.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        pendingPlayers.remove(uuid);
        loadedPlayers.remove(uuid);
    }

    // freeze players who haven't responded yet
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onMove(PlayerMoveEvent event) {
        if (!pendingPlayers.contains(event.getPlayer().getUniqueId())) return;
        // allow head rotation but block movement
        Location from = event.getFrom();
        Location to   = event.getTo();
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            event.setTo(from.clone().setDirection(to.getDirection()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (pendingPlayers.contains(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (pendingPlayers.contains(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventory(InventoryOpenEvent event) {
        if (pendingPlayers.contains(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (!plugin.getConfig().getBoolean("resource-pack.required", true)) return;

        UUID uuid = event.getPlayer().getUniqueId();

        switch (event.getStatus()) {
            case ACCEPTED, DOWNLOADED -> {
                // they accepted and are downloading — just wait, do nothing
            }
            case SUCCESSFULLY_LOADED -> {
                pendingPlayers.remove(uuid);
                loadedPlayers.add(uuid);
                if (plugin.isDebug())
                    plugin.getLogger().info("[ResourcePack] " + event.getPlayer().getName() + " loaded successfully.");
            }
            case DECLINED -> {
                if (!plugin.getConfig().getBoolean("resource-pack.required", true)) return;
                if (loadedPlayers.contains(uuid)) return; // already loaded, ignore
                if (!pendingPlayers.contains(uuid)) return; // spurious event, ignore
                pendingPlayers.remove(uuid);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (event.getPlayer().isOnline() && !loadedPlayers.contains(uuid))
                        event.getPlayer().kick(
                                Component.text("You must accept the resource pack to play on this server.")
                                        .color(NamedTextColor.RED)
                        );
                }, 20L);
            }
            case FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD, DISCARDED -> {
                if (!plugin.getConfig().getBoolean("resource-pack.required", true)) return;
                if (loadedPlayers.contains(uuid)) return; // already loaded, ignore
                if (!pendingPlayers.contains(uuid)) return; // spurious event, ignore
                pendingPlayers.remove(uuid);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (event.getPlayer().isOnline() && !loadedPlayers.contains(uuid))
                        event.getPlayer().kick(
                                Component.text("Resource pack failed to download. Please try rejoining.")
                                        .color(NamedTextColor.RED)
                        );
                }, 20L);
            }
            default -> {}
        }
    }
}