package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Uvogin extends BaseRole {

    private static final long BERSERK_COOLDOWN_TIME = 900000; // 15 minutes en millisecondes
    private static final long BERSERK_DURATION = 2400L; // 2 minutes in ticks
    private static final long NEN_COOLDOWN_TIME = 900000; // 15 minutes en millisecondes
    private boolean isBerserkActive = false;
    private boolean isNenActive = false;
    private static final Map<UUID, Long> berserkCooldowns = new HashMap<>();
    private static final Map<UUID, Long> nenCooldowns = new HashMap<>();

    public Uvogin(Player player, Main plugin) {
        super(player, 2, plugin, "Uvogin\nVous possédez l'effet force 1\n\nItems\nNen: Déclenche une explosion au prochain coup qui infligera 3 cœurs de dégâts supplémentaires à tous les joueurs dans un rayon de 10 blocs, les membres de la Brigade Phantom sont immunisés (15 minutes de cooldown)\nBerserk: Vous octroie l'effet résistance 1 et augmente votre force de 5% pendant 2 minutes (15 minutes de cooldown)\n\nVous gagnez avec la Brigade Phantom");
        giveBerserkItem();
        giveNenItem();
        setForce(15); // Appliquer l'effet de force 1 en permanence
        applyForceEffect(); // Appliquer l'effet de force
    }

    private void giveBerserkItem() {
        ItemStack berserkItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = berserkItem.getItemMeta();
        meta.setDisplayName("Berserk");
        berserkItem.setItemMeta(meta);
        getPlayer().getInventory().addItem(berserkItem);
    }

    private void giveNenItem() {
        ItemStack nenItem = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = nenItem.getItemMeta();
        meta.setDisplayName("Nen");
        nenItem.setItemMeta(meta);
        getPlayer().getInventory().addItem(nenItem);
    }

    public void activateBerserk() {
        Player player = getPlayer();
        UUID playerId = player.getUniqueId();

        // Vérifier le cooldown
        long currentTime = System.currentTimeMillis();
        if (berserkCooldowns.containsKey(playerId)) {
            long lastUseTime = berserkCooldowns.get(playerId);
            long timeLeft = (lastUseTime + BERSERK_COOLDOWN_TIME) - currentTime;

            if (timeLeft > 0) {
                player.sendMessage("Vous devez attendre " + (timeLeft / 1000) + " secondes avant de pouvoir utiliser cette capacité à nouveau.");
                return;
            }
        }

        if (isBerserkActive) {
            player.sendMessage("Berserk est déjà activé.");
            return;
        }

        isBerserkActive = true;
        setForce(force + 5); // Augmenter la force de 5%
        setResistance(15); // Appliquer l'effet de résistance 1
        applyForceEffect();
        applyResistanceEffect();
        berserkCooldowns.put(playerId, currentTime);

        new BukkitRunnable() {
            @Override
            public void run() {
                deactivateBerserk();
            }
        }.runTaskLater(plugin, BERSERK_DURATION); // 2 minutes plus tard
    }

    private void deactivateBerserk() {
        isBerserkActive = false;
        setForce(force - 5); // Réduire la force de 5%
        setResistance(0); // Retirer l'effet de résistance
        applyForceEffect();
        applyResistanceEffect();
        Player player = getPlayer();
        player.sendMessage("Berserk désactivé.");
    }

    public void activateNen() {
        Player player = getPlayer();
        UUID playerId = player.getUniqueId();

        // Vérifier le cooldown
        long currentTime = System.currentTimeMillis();
        if (nenCooldowns.containsKey(playerId)) {
            long lastUseTime = nenCooldowns.get(playerId);
            long timeLeft = (lastUseTime + NEN_COOLDOWN_TIME) - currentTime;

            if (timeLeft > 0) {
                player.sendMessage("Vous devez attendre " + (timeLeft / 1000) + " secondes avant de pouvoir utiliser cette capacité à nouveau.");
                return;
            }
        }

        if (isNenActive) {
            player.sendMessage("Nen est déjà activé.");
            return;
        }

        isNenActive = true;
        nenCooldowns.put(playerId, currentTime);
        player.sendMessage("Nen activé ! Votre prochain coup déclenchera une explosion.");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (isNenActive) {
                    isNenActive = false;
                    player.sendMessage("Nen désactivé.");
                }
            }
        }.runTaskLater(plugin, 600L); // Nen actif pendant 30 secondes
    }

    @Override
    @EventHandler
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        super.handleEntityDamageByEntity(event); // Appeler la méthode de la superclasse
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (player.equals(getPlayer())) {
                if (isNenActive) {
                    isNenActive = false;
                    createNenExplosion(event.getEntity().getLocation());
                }
                double newDamage = calculateDamage(event.getDamage());
                event.setDamage(newDamage);
            }
        }
    }

    private void createNenExplosion(Location location) {
        location.getWorld().playEffect(location, Effect.EXPLOSION_LARGE, 0); // Créer une explosion visuelle

        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 10, 10, 10);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (player.equals(getPlayer())) {
                    continue; // Ignorer Uvogin
                }
                if (plugin.getCustomPlayer(player).getTeam() != 2) {
                    player.damage(6); // 3 cœurs de dégâts
                }
            }
        }
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

    @Override
    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent event) {
        super.handlePlayerDeath(event); // Appeler la méthode de la superclasse
    }
}
