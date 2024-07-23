package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import fr.nathan.plugin.players.CustomPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class BaseRole extends CustomPlayer {

    protected final Main plugin;
    private final String description;

    public BaseRole(Player player, int team, Main plugin, String description) {
        super(player, team);
        this.plugin = plugin;
        this.description = description;
    }

    public void handleEntityDamage(EntityDamageEvent event) {
        // Par défaut, ne rien faire, à override par les sous-classes si nécessaire
    }

    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Par défaut, ne rien faire, à override par les sous-classes si nécessaire
    }

    public String getDescription() {
        return description;
    }

    public abstract void handleInteract(PlayerInteractEvent event);
}
