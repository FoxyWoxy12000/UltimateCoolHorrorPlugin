package net.FoxyWoxy.horrorPlugin.manager;

import net.FoxyWoxy.horrorPlugin.entity.StalkerEntity;
import org.bukkit.entity.Player;

public class PlayerData {

    private final Player        player;
    private StalkerEntity       activeStalker;
    private long                nextSpawnAllowedAt   = 0L;
    private long                gazingAtStalkerSince = -1L;
    private boolean             paranoiaActive       = false;
    private long                paranoiaExpiresAt    = 0L;

    public PlayerData(Player player) { this.player = player; }

    public boolean       hasActiveStalker()           { return activeStalker != null && activeStalker.isAlive(); }
    public StalkerEntity getActiveStalker()            { return activeStalker; }
    public void          setActiveStalker(StalkerEntity s) { this.activeStalker = s; }

    public void clearActiveStalker() {
        if (activeStalker != null && activeStalker.isAlive()) activeStalker.despawn();
        activeStalker = null;
    }

    public boolean isCooldownExpired()    { return System.currentTimeMillis() >= nextSpawnAllowedAt; }
    public void    setCooldown(long secs) { nextSpawnAllowedAt = System.currentTimeMillis() + secs * 1000L; }

    public boolean isGazingAtStalker() { return gazingAtStalkerSince >= 0; }
    public void    startGaze()         { if (gazingAtStalkerSince < 0) gazingAtStalkerSince = System.currentTimeMillis(); }
    public void    clearGaze()         { gazingAtStalkerSince = -1L; }
    public long    gazeElapsedMs()     { return isGazingAtStalker() ? System.currentTimeMillis() - gazingAtStalkerSince : 0L; }

    public boolean isParanoiaActive() {
        if (paranoiaActive && System.currentTimeMillis() > paranoiaExpiresAt) paranoiaActive = false;
        return paranoiaActive;
    }

    public void applyParanoia(long durationSeconds) {
        long now = System.currentTimeMillis();
        if (paranoiaActive && paranoiaExpiresAt > now) paranoiaExpiresAt += durationSeconds * 1000L;
        else { paranoiaActive = true; paranoiaExpiresAt = now + durationSeconds * 1000L; }
    }

    public long paranoiaRemainingSeconds() {
        if (!isParanoiaActive()) return 0;
        return Math.max(0, (paranoiaExpiresAt - System.currentTimeMillis()) / 1000L);
    }

    public Player getPlayer() { return player; }
}