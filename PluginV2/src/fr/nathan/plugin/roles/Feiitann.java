package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Feiitann extends BaseRole {

    private double totalDamageReceived = 0;
    private int currentTier = 0;
    private boolean isNight = false;
    private boolean powerActivated = false;
    private boolean fireAspectActive = false;

    public Feiitann(Player player, Main plugin) {
        super(player, 2, plugin, "Feiitann\n\n" +
                "Vous avez l'effet Speed 1 permanent.\n\n" +
                "Vous gagnez avec l'équipe 2.");
        setVitesse(20); // Initialement Speed 1
    }


    @Override
    @EventHandler
    public void handleEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
            if (entityDamageByEntityEvent.getDamager() instanceof Player) {
                Player damager = (Player) entityDamageByEntityEvent.getDamager();
                Player player = getPlayer();
                if (event.getEntity() instanceof Player && player.equals(event.getEntity())) {
                    double damage = event.getFinalDamage();
                    double effectiveDamage = calculateEffectiveDamage(player, damage);

                    totalDamageReceived += effectiveDamage;
                    if (!powerActivated) {
                        checkTier();
                        if (player.getHealth() - damage <= 4) {
                            activatePower();
                        }
                    }
                }
            }
        }
    }

    private double calculateEffectiveDamage(Player player, double damage) {
        double health = player.getHealth();
        double absorptionHearts = 0.0;

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.ABSORPTION)) {
                absorptionHearts += (effect.getAmplifier() + 1) * 2;
            }
        }

        return Math.min(damage, health + absorptionHearts);
    }

    private void checkTier() {
        int newTier = (int) (totalDamageReceived / 20); // Chaque palier est atteint pour chaque 20 dégâts
        if (newTier > currentTier) {
            currentTier = newTier;
            getPlayer().sendMessage(getTierMessage(currentTier));
        }
    }

    private String getTierMessage(int tier) {
        switch (tier) {
            case 1:
                return "Vous avez atteint le palier 1: +2 coeurs permanents";
            case 2:
                return "Vous avez atteint le palier 2: +2 coeurs permanents";
            case 3:
                return "Vous avez atteint le palier 3: +2 coeurs permanents";
            case 4:
                return "Vous avez atteint le palier 4: +4 coeurs permanents, force 1 de nuit et vos coups sont enflammés";
            case 5:
                return "Vous avez atteint le palier 5: +4 coeurs permanents, force 1 permanent, résistance de jour et vos coups sont enflammés";
            default:
                return null;
        }
    }

    public void activatePower() {
        if (powerActivated) {
            getPlayer().sendMessage("Votre pouvoir a déjà été activé.");
            return;
        }

        powerActivated = true;
        Player player = getPlayer();
        applyTierEffects(currentTier);
        player.setHealth(Math.min(player.getHealth() + 10, player.getMaxHealth())); // Heal 5 hearts
    }


    private void applyTierEffects(int tier) {
        Player player = getPlayer();
        switch (tier) {
            case 1:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 0, false, false)); // +2 hearts
                break;
            case 2:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 0, false, false)); // +2 hearts total
                applyNightStrength();
                break;
            case 3:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 0, false, false)); // +2 hearts total
                applyNightStrength();
                applyFireAspect();
                break;
            case 4:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 1, false, false)); // +4 hearts total
                applyNightStrength();
                applyFireAspect();
                break;
            case 5:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 1, false, false)); // +4 hearts total
                setForce(15);              
                applyFireAspect();
                applyNightResistance();
                break;
        }
    }

    private void applyNightStrength() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (player.getWorld().getTime() >= 13000 && player.getWorld().getTime() <= 23000) { // Vérifier si c'est la nuit
                	if(isNight==false) {
                		setForce(force + 15); // Strength 1 de nuit
                		isNight=true;
                	}
                } else {
                	if(isNight==true) {
                		setForce(force - 15); // Pas de force le jour
                		isNight=false;
                	}
                }
            }
        }.runTaskTimer(plugin, 0L, 80L); // Vérifier toutes les 4 secondes
    }
    private void applyNightResistance() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (player.getWorld().getTime() >= 13000 && player.getWorld().getTime() <= 23000) { // Vérifier si c'est la nuit
                	if(isNight==false) {
                		setResistance(resistance + 15);
                		isNight=true;
                	}
                } else {
                	if(isNight==true) {
                		setResistance(resistance - 15); 
                		isNight=false;
                	}
                }
            }
        }.runTaskTimer(plugin, 0L, 80L); // Vérifier toutes les 4 secondes
    }



    private void applyFireAspect() {
        fireAspectActive = true;
    }

    @Override
    @EventHandler
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (player.equals(getPlayer())) {
                // Calculer les dégâts modifiés
                double newDamage = calculateDamage(event.getDamage());
                event.setDamage(newDamage);

                // Appliquer l'effet de Fire Aspect
                if (fireAspectActive && event.getEntity() instanceof Player) {
                    Player target = (Player) event.getEntity();
                    target.setFireTicks(100); // Enflamme l'adversaire pendant 5 secondes
                }
            }
        }
    }

    @Override
    @EventHandler
    public void handleInteract(PlayerInteractEvent event) {
        // No special items for Feiitann
    }

    @Override
    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent event) {
        // Implémentez la logique pour gérer la mort du joueur ici
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
