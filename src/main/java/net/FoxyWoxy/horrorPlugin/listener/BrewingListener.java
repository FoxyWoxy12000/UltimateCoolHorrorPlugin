package net.FoxyWoxy.horrorPlugin.listener;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.item.CursedEyeItem;
import net.FoxyWoxy.horrorPlugin.item.ParanoiaBrew;
import net.FoxyWoxy.horrorPlugin.manager.PlayerData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class BrewingListener implements Listener {

    private final HorrorPlugin  plugin;
    private final CursedEyeItem cursedEye;
    private final ParanoiaBrew  paranoiaBrew;

    public BrewingListener(HorrorPlugin plugin) {
        this.plugin       = plugin;
        this.cursedEye    = plugin.getCursedEyeItem();
        this.paranoiaBrew = new ParanoiaBrew(plugin);
        plugin.getServer().getPluginManager().registerEvents(new ConsumeListener(), plugin);
    }

    // Allow placing the cursed eye into the brewing stand ingredient slot
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof BrewerInventory brewerInv)) return;

        ItemStack cursor = event.getCursor();
        if (!cursedEye.isCursedEye(cursor)) return;

        // slot 3 is the ingredient slot in a brewing stand
        if (event.getRawSlot() != 3) return;

        event.setCancelled(true);

        // manually place the item into the ingredient slot
        ItemStack existing = brewerInv.getIngredient();
        brewerInv.setIngredient(cursor.clone());

        // give back whatever was in the slot, or clear cursor
        if (existing != null && existing.getType() != Material.AIR) {
            event.getWhoClicked().setItemOnCursor(existing);
        } else {
            event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
        }
    }

    // Prevent dragging into brewing stand ingredient slot too
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory() instanceof BrewerInventory)) return;
        if (event.getRawSlots().contains(3)) {
            ItemStack item = event.getOldCursor();
            if (cursedEye.isCursedEye(item)) event.setCancelled(true);
        }
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

        // cancel the vanilla brew so it doesn't try to process it normally
        event.setCancelled(true);

        // simulate brew time then produce the result
        new BukkitRunnable() {
            int ticks = 0;
            final int brewTime = 400; // 20 seconds like vanilla

            @Override
            public void run() {
                ticks += 10;
                if (ticks < brewTime) return;

                cancel();

                // check ingredient is still there
                if (!cursedEye.isCursedEye(inv.getIngredient())) return;

                for (int i = 0; i < 3; i++) {
                    ItemStack b = inv.getItem(i);
                    if (b != null && b.getType() == Material.POTION)
                        inv.setItem(i, paranoiaBrew.createItem());
                }

                // consume one cursed eye
                ItemStack ing = inv.getIngredient();
                if (ing != null && ing.getAmount() > 1) ing.setAmount(ing.getAmount() - 1);
                else inv.setIngredient(null);
            }
        }.runTaskTimer(plugin, 10L, 10L);
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