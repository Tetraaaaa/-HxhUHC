package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

public class Hanzo extends BaseRole {

    private boolean isInvisible = false;
    private Location invisibilityCenter;
    private static final long COOLDOWN_TIME = 300000; // 5 minutes en millisecondes
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public Hanzo(Player player, Main plugin) {
        super(player, 1, plugin, "Hanzo\n\n" +
                "Vous avez l'effet Speed 2 permanent.\n\n" +
                "Items\n" +
                "Fumigène: Vous permet avec un click droit de vous rendre invisible dans un rayon de 15 blocks, vous redevenez visible en sortant de la zone ou en infligeant un dégât.\n\n" +
                "Vous gagnez avec les Hunters.");
        giveFumigeneItem();
        setVitesse(40); // Appliquer l'effet de speed 2 en permanence
        applySpeedEffect(); // Appliquer l'effet de speed
    }

    private void giveFumigeneItem() {
        ItemStack fumigene = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = fumigene.getItemMeta();
        meta.setDisplayName("Fumigène");
        fumigene.setItemMeta(meta);
        getPlayer().getInventory().addItem(fumigene);
    }

    public void activateInvisibility() {
        Player player = getPlayer();
        UUID playerId = player.getUniqueId();

        // Vérifiez le cooldown
        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(playerId)) {
            long lastUseTime = cooldowns.get(playerId);
            long timeLeft = (lastUseTime + COOLDOWN_TIME) - currentTime;

            if (timeLeft > 0) {
                player.sendMessage("Vous devez attendre " + (timeLeft / 1000) + " secondes avant de pouvoir utiliser cette capacité à nouveau.");
                return;
            }
        }

        // Appliquez l'invisibilité et démarrez le cooldown
        isInvisible = true;
        invisibilityCenter = player.getLocation();
        applyInvisibilityEffect(player);
        cooldowns.put(playerId, currentTime);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isInvisible) {
                    cancel();
                    return;
                }

                if (!player.isOnline() || player.getLocation().distance(invisibilityCenter) > 15) {
                    deactivateInvisibility();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Check every second

        new BukkitRunnable() {
            @Override
            public void run() {
                if (isInvisible) {
                    deactivateInvisibility();
                }
            }
        }.runTaskLater(plugin, 300L); // 15 seconds later
    }

    private void applyInvisibilityEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 300, 1, false, false), true); // 15 seconds
    }

    public void deactivateInvisibility() {
        isInvisible = false;
        Player player = getPlayer();
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.sendMessage("Vous êtes redevenu visible.");
    }

    @Override
    @EventHandler
    public void handleInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.NETHER_STAR && item.getItemMeta().getDisplayName().equals("Fumigène")) {
            activateInvisibility();
            event.setCancelled(true); // Annule l'événement pour éviter d'autres actions
        }
    }

    @Override
    @EventHandler
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        super.handleEntityDamageByEntity(event); // Appeler la méthode de la superclasse
        if (isInvisible) {
            deactivateInvisibility();
        }
        // Calculer les dégâts modifiés
        double newDamage = calculateDamage(event.getDamage());
        event.setDamage(newDamage);
    }

    @Override
    @EventHandler
    public void handleEntityDamage(EntityDamageEvent event) {
        super.handleEntityDamage(event); // Appeler la méthode de la superclasse
    }

    @Override
    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent event) {
        super.handlePlayerDeath(event); // Appeler la méthode de la superclasse
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
}
