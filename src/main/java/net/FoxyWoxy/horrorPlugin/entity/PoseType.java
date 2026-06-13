package net.FoxyWoxy.horrorPlugin.entity;

public enum PoseType {

    // ── Open area poses ──────────────────────────────────────────
    STANDING        ("stalker_standing"),       // upright, far away
    TILTED          ("stalker_tilted"),          // head tilted unnaturally
    REACHING        ("stalker_reaching"),        // one arm extended toward player
    SITTING         ("stalker_sitting"),         // seated on ground watching

    // ── Wall / corner poses ──────────────────────────────────────
    PEEK_LEFT       ("stalker_peek_left"),       // leaning around left side of wall
    PEEK_RIGHT      ("stalker_peek_right"),      // leaning around right side of wall
    PRESSED_WALL    ("stalker_pressed_wall"),    // flat against a wall facing player

    // ── Low ceiling / slab poses ─────────────────────────────────
    UNDER_SLAB      ("stalker_under_slab"),      // crouched under a slab, looking out
    OVER_SLAB       ("stalker_over_slab"),       // peering over the top of a slab
    CRAWLING        ("stalker_crawling"),        // fully prone, crawling toward player

    // ── Ceiling / elevated poses ─────────────────────────────────
    HANGING         ("stalker_hanging"),         // hanging from fence/ceiling by hands
    HANGING_INVERTED("stalker_hanging_inverted"),// fully upside down from ceiling
    CLINGING_WALL   ("stalker_clinging_wall"),   // spider-like on a vertical wall

    // ── Doorframe / tight space poses ───────────────────────────
    DOORFRAME       ("stalker_doorframe"),       // standing in a doorway
    CROUCHING       ("stalker_crouching");       // crouched in a low passage

    private final String itemModelName;

    PoseType(String itemModelName) {
        this.itemModelName = itemModelName;
    }

    public String getItemModelName() { return itemModelName; }
}