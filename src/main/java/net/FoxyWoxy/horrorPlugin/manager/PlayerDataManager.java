package net.FoxyWoxy.horrorPlugin.manager;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class PlayerDataManager implements Listener {

    private final HorrorPlugin              plugin;
    private final Map<UUID, PlayerData>     dataMap = new HashMap<>();

    public PlayerDataManager(HorrorPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player p : plugin.getServer().getOnlinePlayers())
            dataMap.put(p.getUniqueId(), new PlayerData(p));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        dataMap.put(e.getPlayer().getUniqueId(), new PlayerData(e.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerData d = dataMap.remove(e.getPlayer().getUniqueId());
        if (d != null) d.clearActiveStalker();
    }

    public PlayerData            get(Player p) { return dataMap.computeIfAbsent(p.getUniqueId(), id -> new PlayerData(p)); }
    public Collection<PlayerData> all()        { return dataMap.values(); }
}