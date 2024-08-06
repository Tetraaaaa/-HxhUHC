package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class BaseRole {

    protected Player player;
    protected int team;
    protected Main plugin;
    protected String description;
    protected int force;
    protected int resistance;
    protected int vitesse;

    public BaseRole(Player player, int team, Main plugin, String description) {
        this.player = player;
        this.team = team;
        this.plugin = plugin;
        this.description = description;
        this.force = 0;
        this.resistance = 0;
        this.vitesse = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public int getTeam() {
        return team;
    }

    public String getDescription() {
        return description;
    }

    public void setForce(int force) {
        this.force = force;
        applyForceEffect();
    }

    public void setResistance(int resistance) {
        this.resistance = resistance;
        applyResistanceEffect();
    }

    public void setVitesse(int vitesse) {
        this.vitesse = vitesse;
        applySpeedEffect();
    }

    public abstract void applyForceEffect();

    public abstract void applyResistanceEffect();

    public abstract void applySpeedEffect();

    public void handleEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
            Player damager = (Player) entityDamageByEntityEvent.getDamager();
            if (damager.equals(getPlayer())) {
                double newDamage = calculateDamage(event.getDamage());
                event.setDamage(newDamage);
            }
        }
    }

    public void handleInteract(PlayerInteractEvent event) {
        // Logique de base pour gérer l'interaction du joueur
    }

    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Logique de base pour gérer les dommages infligés par l'entité
    }

    public void handlePlayerDeath(PlayerDeathEvent event) {
        // Logique de base pour gérer la mort du joueur
    }

    public double calculateDamage(double originalDamage) {
        double factor;
        if (force >= 30) {
            factor = 3.3 - (force / 100.0);
        } else if (force >= 15) {
            factor = 2.3 - (force / 100.0);
        } else {
            factor = 1.0;
        }
        return originalDamage / factor;
    }
}
