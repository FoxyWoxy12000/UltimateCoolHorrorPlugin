package net.FoxyWoxy.horrorPlugin;

import net.FoxyWoxy.horrorPlugin.item.CursedEyeItem;
import net.FoxyWoxy.horrorPlugin.listener.BrewingListener;
import net.FoxyWoxy.horrorPlugin.listener.PlayerJoinListener;
import net.FoxyWoxy.horrorPlugin.listener.PlayerLookListener;
import net.FoxyWoxy.horrorPlugin.manager.PlayerDataManager;
import net.FoxyWoxy.horrorPlugin.manager.ResourcePackManager;
import net.FoxyWoxy.horrorPlugin.manager.StalkerManager;
import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.FoxyWoxy.horrorPlugin.commands.HorrorCommand;
import net.FoxyWoxy.horrorPlugin.listener.ResourcePackListener;

public final class HorrorPlugin extends JavaPlugin {

    private static HorrorPlugin instance;
    public static HorrorPlugin getInstance() { return instance; }

    private PlayerDataManager   playerDataManager;
    private StalkerManager      stalkerManager;
    private ResourcePackManager resourcePackManager;
    private CursedEyeItem       cursedEyeItem;

    @Override
    public void onLoad() {
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        PacketEvents.getAPI().init();

        playerDataManager   = new PlayerDataManager(this);
        resourcePackManager = new ResourcePackManager(this);
        stalkerManager      = new StalkerManager(this);
        cursedEyeItem       = new CursedEyeItem(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLookListener(this), this);
        getServer().getPluginManager().registerEvents(new BrewingListener(this), this);
        getServer().getPluginManager().registerEvents(new ResourcePackListener(this), this);
        getServer().getScheduler().runTaskTimer(this, stalkerManager::tick, 20L, 10L);
        getCommand("foxyshorrorplugin").setExecutor(new HorrorCommand(this));
        getCommand("foxyshorrorplugin").setTabCompleter(new HorrorCommand(this));
        getLogger().info("HorrorPlugin enabled. They're watching.");
    }

    @Override
    public void onDisable() {
        if (stalkerManager != null) stalkerManager.despawnAll();
        PacketEvents.getAPI().terminate();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("horror")) return false;
        if (!sender.hasPermission("horror.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }
        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                reloadConfig();
                stalkerManager.reloadConfig();
                sender.sendMessage("§aConfig reloaded.");
            }
            case "give" -> {
                Player t = resolvePlayer(sender, args);
                if (t == null) return true;
                t.getInventory().addItem(cursedEyeItem.createItem());
                sender.sendMessage("§aGave Cursed Eye to " + t.getName());
            }
            case "debug" -> {
                boolean cur = getConfig().getBoolean("debug.enabled");
                getConfig().set("debug.enabled", !cur);
                sender.sendMessage("§eDebug: " + (!cur ? "§aON" : "§cOFF"));
            }
            case "spawn" -> {
                Player t = resolvePlayer(sender, args);
                if (t == null) return true;
                stalkerManager.forceSpawn(t);
                sender.sendMessage("§eStalker force-spawned on " + t.getName());
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e/horror reload|give [p]|debug|spawn [p]");
    }

    private Player resolvePlayer(CommandSender sender, String[] args) {
        if (args.length > 1) {
            Player p = getServer().getPlayer(args[1]);
            if (p == null) { sender.sendMessage("§cPlayer not found."); return null; }
            return p;
        }
        if (sender instanceof Player p) return p;
        sender.sendMessage("§cSpecify a player.");
        return null;
    }

    public PlayerDataManager   getPlayerDataManager()   { return playerDataManager; }
    public StalkerManager      getStalkerManager()       { return stalkerManager; }
    public ResourcePackManager getResourcePackManager()  { return resourcePackManager; }
    public CursedEyeItem       getCursedEyeItem()        { return cursedEyeItem; }
    public boolean isDebug() { return getConfig().getBoolean("debug.enabled", false); }
}