package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Hisoka extends BaseRole {

    private static final long BUNGEE_GUM_COOLDOWN = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final long DAMAGE_TRACKING_DURATION = 30 * 1000; // 30 seconds in milliseconds
    private static final double MAX_DISTANCE = 30.0; // 30 blocks
    private static final double DAMAGE_REDUCTION_INCREMENT = 0.07; // Increment to reduce the damage reduction factor by 0.07 on each kill

    private final Map<UUID, Long> damageTracking = new HashMap<>();
    private long lastBungeeGumUse = 0;

    public Hisoka(Player player, Main plugin) {
        super(player, 3, plugin, "Hisoka\n\n" +
                "Vous gagnez avec l'équipe 3.");
        giveBungeeGumItem();
        setForce(15); // Appliquer l'effet de force 1 en permanence
        applyForceEffect(); // Appliquer l'effet de force
    }

    private void giveBungeeGumItem() {
        Player player = getPlayer();
        ItemStack bungeeGum = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = bungeeGum.getItemMeta();
        meta.setDisplayName("Bungee Gum");
        bungeeGum.setItemMeta(meta);
        player.getInventory().addItem(bungeeGum);
    }

    @Override
    @EventHandler
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        super.handleEntityDamageByEntity(event); // Appeler la méthode de la superclasse
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (damager.equals(getPlayer()) && event.getEntity() instanceof Player) {
                Player target = (Player) event.getEntity();
                damageTracking.put(target.getUniqueId(), System.currentTimeMillis());
                // Réduire les dégâts 
                double newDamage = calculateDamage(event.getDamage());
                event.setDamage(newDamage);
            }
        }
    }

    @Override
    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent event) {
        super.handlePlayerDeath(event); // Appeler la méthode de la superclasse
        Player deceased = event.getEntity();
        Player killer = deceased.getKiller();
        if (killer != null && killer.equals(getPlayer())) {
            setForce(force + 7); // Augmenter la force de 7%
            applyForceEffect(); // Appliquer l'effet de force
            getPlayer().sendMessage("Vous obtenez +7% de force ");
        }
    }

    @Override
    @EventHandler
    public void handleInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.BLAZE_POWDER && item.getItemMeta().getDisplayName().equals("Bungee Gum")) {
            Player player = getPlayer();
            if (!player.equals(event.getPlayer())) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBungeeGumUse < BUNGEE_GUM_COOLDOWN) {
                player.sendMessage("Cooldown: " + ((BUNGEE_GUM_COOLDOWN - (currentTime - lastBungeeGumUse)) / 1000) + " seconds.");
                return;
            }

            Player target = getNearestDamagedPlayer();
            if (target != null) {
                player.sendMessage("Bungee Gum activé sur " + target.getName());
                target.teleport(player.getLocation());
                lastBungeeGumUse = currentTime;
            } else {
                player.sendMessage("Aucune cible valide.");
            }

            event.setCancelled(true);
        }
    }

    private Player getNearestDamagedPlayer() {
        Player player = getPlayer();
        long currentTime = System.currentTimeMillis();
        Player nearestPlayer = null;
        double nearestDistance = MAX_DISTANCE;

        for (Map.Entry<UUID, Long> entry : damageTracking.entrySet()) {
            UUID targetId = entry.getKey();
            long damageTime = entry.getValue();

            if (currentTime - damageTime > DAMAGE_TRACKING_DURATION) {
                continue; // Ignore targets damaged more than 30 seconds ago
            }

            Player target = Bukkit.getPlayer(targetId);
            if (target != null && target.isOnline() && !target.equals(player)) {
                double distance = player.getLocation().distance(target.getLocation());
                if (distance <= MAX_DISTANCE && distance < nearestDistance) {
                    nearestPlayer = target;
                    nearestDistance = distance;
                }
            }
        }

        return nearestPlayer;
    }

    @Override
    public void applyForceEffect() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                if (force >= 15 && force < 30) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0, false, false)); // Appliquer pendant 5 secondes (100 ticks)
                } else if (force >= 30) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1, false, false)); // Appliquer pendant 5 secondes (100 ticks)
                }
            }
        }.runTaskTimer(plugin, 0L, 60L); // Répéter toutes les 60 ticks (3 secondes)
    }


    @Override
    public void applyResistanceEffect() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                if (resistance >= 15) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 0, false, false)); // Appliquer pendant 5 secondes (100 ticks)
                } else if (resistance >= 30) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 1, false, false)); // Appliquer pendant 5 secondes (100 ticks)
                }
            }
        }.runTaskTimer(plugin, 0L, 60L); // Répéter toutes les 60 ticks (3 secondes)
    }


    @Override
    public void applySpeedEffect() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                player.removePotionEffect(PotionEffectType.SPEED);
                if (vitesse >= 20 && vitesse < 40) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, false, false)); // Appliquer pendant 5 secondes (100 ticks)
                } else if (vitesse >= 40 && vitesse < 60) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false)); // Appliquer pendant 5 secondes (100 ticks)
                } else if (vitesse >= 60) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, false, false)); // Appliquer pendant 5 secondes (100 ticks)
                }
            }
        }.runTaskTimer(plugin, 0L, 60L); // Répéter toutes les 60 ticks (3 secondes)
    }

    @Override
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

    @Override
    @EventHandler
    public void handleEntityDamage(EntityDamageEvent event) {
        super.handleEntityDamage(event); // Appeler la méthode de la superclasse
    }

   
}
