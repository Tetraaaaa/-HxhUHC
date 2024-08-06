package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import fr.nathan.plugin.utils.ActionBarUtil;
import fr.nathan.plugin.utils.VisibilityUtil;
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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Kirua extends BaseRole {

    private static final int MAX_ELECTRIC_POINTS = 100;
    private static final int ELECTRIC_RECHARGE_RATE = 3; // Points recharge every 10 seconds
    private static final int ELECTRIC_RECHARGE_INTERVAL = 200; // 10 seconds in ticks
    private static final int ELECTRIC_CONSUMPTION_SPEED = 50;
    private static final int ELECTRIC_CONSUMPTION_PASSIVE = 5;
    private static final int INVISIBILITY_RADIUS = 7;
    private static final int INVISIBILITY_CHECK_INTERVAL = 20; // Check every second

    private int electricPoints = MAX_ELECTRIC_POINTS;
    private int hitCounter = 0;
    private boolean isInvisible = false;
    private boolean divineSpeedActive = false;
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public Kirua(Player player, Main plugin) {
        super(player, 1, plugin, "Kirua\n\n" +
                "Vous avez les effets Speed 1 et Haste 1 permanents.\n\n" +
                "Utilisez vos points électriques pour activer des compétences spéciales.\n\n" +
                "Recharge automatique des points toutes les 10 secondes.\n\n" +
                "Vitesse divine : Nether Star (consomme 50 points électriques).\n" +
                "Passif : Immobilise l'adversaire tous les 5 coups (consomme 5 points électriques).\n" +
                "/hxh invi : Devenez invisible la nuit.");
        applyPermanentEffects();
        startElectricRechargeTask();
        giveDivineSpeedItem();
        startActionBarUpdateTask();
    }

    private void applyPermanentEffects() {
        setVitesse(20); // Speed 1
    }

    private void startElectricRechargeTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                electricPoints = Math.min(electricPoints + ELECTRIC_RECHARGE_RATE, MAX_ELECTRIC_POINTS);
                updateElectricPointsActionBar();
            }
        }.runTaskTimer(plugin, 0L, ELECTRIC_RECHARGE_INTERVAL);
    }

    private void giveDivineSpeedItem() {
        Player player = getPlayer();
        ItemStack divineSpeed = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = divineSpeed.getItemMeta();
        meta.setDisplayName("Vitesse divine");
        divineSpeed.setItemMeta(meta);
        player.getInventory().addItem(divineSpeed);
    }

    private void activateDivineSpeed() {
        Player player = getPlayer();
        if (electricPoints >= ELECTRIC_CONSUMPTION_SPEED) {
            divineSpeedActive = true;
            electricPoints -= ELECTRIC_CONSUMPTION_SPEED;
            player.sendMessage("Vitesse divine activée. Points restants : " + electricPoints);
            updateElectricPointsActionBar();

            // Remove base speed effect and apply Speed 3
            player.removePotionEffect(PotionEffectType.SPEED);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 2, false, false)); // Speed 3 for 1 minute

            new BukkitRunnable() {
                @Override
                public void run() {
                    divineSpeedActive = false;
                    applySpeedEffect(); // Reapply base speed effect
                }
            }.runTaskLater(plugin, 1200L); // Reset divineSpeedActive after 1 minute (1200 ticks)
        } else {
            player.sendMessage("Pas assez de points électriques.");
        }
    }

    public void applyInvisibility() {
        Player player = getPlayer();
        if (!isInvisible && player.getWorld().getTime() >= 13000 && player.getWorld().getTime() <= 23000) { // Check if it's night
            isInvisible = true;
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
            VisibilityUtil.setPlayerInvisible(player, true);
            player.sendMessage("Vous êtes maintenant invisible.");
            startInvisibilityCheckTask();
        }
    }

    private void startInvisibilityCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (!isInvisible) {
                    cancel();
                    return;
                }

                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (other.equals(player)) continue;
                    if (other.getLocation().distance(player.getLocation()) <= INVISIBILITY_RADIUS) {
                        VisibilityUtil.setPlayerInvisible(player, false);
                        player.removePotionEffect(PotionEffectType.INVISIBILITY); // Remove invisibility effect
                        player.sendMessage("Vous êtes redevenu visible.");
                        isInvisible = false;
                        cancel();
                        return;
                    }
                }

                if (player.getWorld().getTime() < 13000 || player.getWorld().getTime() > 23000) {
                    VisibilityUtil.setPlayerInvisible(player, false);
                    player.removePotionEffect(PotionEffectType.INVISIBILITY); // Remove invisibility effect
                    player.sendMessage("Vous êtes redevenu visible.");
                    isInvisible = false;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, INVISIBILITY_CHECK_INTERVAL);
    }

    private void startActionBarUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                ActionBarUtil.sendActionBar(player, "Points Électriques : " + electricPoints);
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }

    private void updateElectricPointsActionBar() {
        Player player = getPlayer();
        ActionBarUtil.sendActionBar(player, "Points Électriques : " + electricPoints);
    }

    @Override
    @EventHandler
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(getPlayer())) {
            hitCounter++;
            if (hitCounter >= 5 && electricPoints >= ELECTRIC_CONSUMPTION_PASSIVE) {
                hitCounter = 0;
                electricPoints -= ELECTRIC_CONSUMPTION_PASSIVE;
                Player target = (Player) event.getEntity();
                target.sendMessage("Vous avez été immobilisé par Kirua.");
                applyKnockback(target); // Simulate the freeze effect using knockback
                updateElectricPointsActionBar();
            }
            // Calculer les dégâts modifiés
            double newDamage = calculateDamage(event.getDamage());
            event.setDamage(newDamage);
        }
    }

    private void applyKnockback(Player target) {
        Vector knockback = target.getVelocity().multiply(-0.1); // Apply a small knockback in the opposite direction
        target.setVelocity(knockback);
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

    @Override
    @EventHandler
    public void handleInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.NETHER_STAR && item.getItemMeta().getDisplayName().equals("Vitesse divine")) {
            activateDivineSpeed();
            event.getPlayer().getInventory().remove(item); // Remove the item after use
            event.setCancelled(true); // Annule l'événement pour éviter d'autres actions
        }
    }

    public void onCommand(Player player, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("invi")) {
            applyInvisibility();
        }
    }
}
