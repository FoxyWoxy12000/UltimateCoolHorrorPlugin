package net.FoxyWoxy.horrorPlugin.listener;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.item.CursedEyeItem;
import net.FoxyWoxy.horrorPlugin.item.ParanoiaBrew;
import net.FoxyWoxy.horrorPlugin.manager.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

public class BrewingListener
        implements Listener {

    private final HorrorPlugin  plugin;
    private final CursedEyeItem cursedEye;
    private final ParanoiaBrew  paranoiaBrew;

    public BrewingListener(HorrorPlugin plugin) {
        this.plugin       = plugin;
        this.cursedEye    = plugin.getCursedEyeItem();
        this.paranoiaBrew = new ParanoiaBrew(plugin);
        plugin.getServer().getPluginManager().registerEvents(new ConsumeListener(), plugin);
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        BrewerInventory inv        = event.getContents();
        ItemStack       ingredient = inv.getIngredient();
        if (!cursedEye.isCursedEye(ingredient)) return;

        boolean anyPotion = false;
        for (int i = 0; i < 3; i++) {
            ItemStack b = inv.getItem(i);
            if (b != null && b.getType() == Material.POTION) { anyPotion = true; break; }
        }
        if (!anyPotion) return;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (int i = 0; i < 3; i++) {
                ItemStack b = inv.getItem(i);
                if (b != null && b.getType() == Material.POTION)
                    inv.setItem(i, paranoiaBrew.createItem());
            }
            ItemStack ing = inv.getIngredient();
            if (ing != null && ing.getAmount() > 1) ing.setAmount(ing.getAmount() - 1);
            else inv.setIngredient(null);
        });
    }

    private class ConsumeListener implements Listener {
        @EventHandler
        public void onConsume(PlayerItemConsumeEvent event) {
            if (!paranoiaBrew.isParanoiaBrew(event.getItem())) return;
            PlayerData data = plugin.getPlayerDataManager().get(event.getPlayer());
            paranoiaBrew.applyEffect(data);
        }
    }
}