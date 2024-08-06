package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class RoleManager implements Listener {

    private final Main plugin;
    private final Map<Player, BaseRole> playerRoles = new HashMap<>();

    public RoleManager(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void assignRole(Player player, BaseRole role) {
        playerRoles.put(player, role);
    }

    public BaseRole getRole(Player player) {
        return playerRoles.get(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!plugin.isDamageEnabled() && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            BaseRole role = playerRoles.get(player);
            if (role != null) {
                role.handleEntityDamage(event);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BaseRole role = playerRoles.get(player);
        if (role != null) {
            role.handleInteract(event);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            BaseRole role = playerRoles.get(player);
            if (role != null) {
                role.handleEntityDamageByEntity(event);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        BaseRole role = playerRoles.get(player);
        if (role != null) {
            role.handlePlayerDeath(event);
        }
    }
}
