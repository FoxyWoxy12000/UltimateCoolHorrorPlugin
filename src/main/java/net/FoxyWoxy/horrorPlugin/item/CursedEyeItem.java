package net.FoxyWoxy.horrorPlugin.item;

import net.FoxyWoxy.horrorPlugin.HorrorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class CursedEyeItem {

    public static final String PDC_KEY = "cursed_eye";

    private final HorrorPlugin  plugin;
    private final NamespacedKey pdcKey;
    private final NamespacedKey modelKey;

    public CursedEyeItem(HorrorPlugin plugin) {
        this.plugin   = plugin;
        this.pdcKey   = new NamespacedKey(plugin, PDC_KEY);
        this.modelKey = new NamespacedKey("horrorplugin", "cursed_eye");
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta  meta = item.getItemMeta();

        meta.setItemModel(modelKey);

        meta.displayName(Component.text("Cursed Eye")
                .color(NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));

        meta.lore(List.of(
                Component.text("It stares back.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, true),
                Component.empty(),
                Component.text("Used in Paranoia Brew crafting.")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isCursedEye(ItemStack item) {
        if (item == null || item.getType() != Material.RECOVERY_COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(pdcKey, PersistentDataType.BYTE);
    }

    public NamespacedKey getPdcKey() { return pdcKey; }
}