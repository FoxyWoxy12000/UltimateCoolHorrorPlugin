package net.FoxyWoxy.horrorPlugin.entity;

public enum PoseType {
    STANDING  (1001, "Standing upright in the distance"),
    PEEKING   (1002, "Leaning around a wall corner"),
    CROUCHING (1003, "Hidden under a slab or low ceiling"),
    HANGING   (1004, "Suspended from a fence or ceiling beam"),
    SITTING   (1005, "Seated, legs folded, watching"),
    TILTED    (1006, "Head tilted at an unnatural angle"),
    REACHING  (1007, "One arm extended toward the player");

    private final int    customModelData;
    private final String description;

    PoseType(int cmd, String desc) {
        this.customModelData = cmd;
        this.description     = desc;
    }

    public int    getCustomModelData() { return customModelData; }
    public String getDescription()     { return description; }
}