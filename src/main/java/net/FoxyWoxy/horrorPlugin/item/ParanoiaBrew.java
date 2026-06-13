package net.FoxyWoxy.horrorPlugin.item;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.FoxyWoxy.horrorPlugin.manager.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ParanoiaBrew {

    public static final String PDC_KEY = "paranoia_brew";

    private final HorrorPlugin  plugin;
    private final NamespacedKey pdcKey;
    private final NamespacedKey modelKey;

    public ParanoiaBrew(HorrorPlugin plugin) {
        this.plugin   = plugin;
        this.pdcKey   = new NamespacedKey(plugin, PDC_KEY);
        this.modelKey = new NamespacedKey("horrorplugin", "paranoia_brew");
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta  meta = item.getItemMeta();

        meta.setItemModel(modelKey);

        long duration = plugin.getConfig().getLong("paranoia-potion.duration", 120L);

        meta.displayName(Component.text("Paranoia Brew")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));

        meta.lore(List.of(
                Component.text("They come more often now.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, true),
                Component.empty(),
                Component.text("Duration: " + duration + "s")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isParanoiaBrew(ItemStack item) {
        if (item == null || item.getType() != Material.RECOVERY_COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(pdcKey, PersistentDataType.BYTE);
    }

    public void applyEffect(PlayerData data) {
        long duration = plugin.getConfig().getLong("paranoia-potion.duration", 120L);
        data.applyParanoia(duration);
        data.getPlayer().sendMessage(Component.text("Your vision blurs. Something watches.")
                .color(NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, true));
    }

    public NamespacedKey getPdcKey() { return pdcKey; }
}