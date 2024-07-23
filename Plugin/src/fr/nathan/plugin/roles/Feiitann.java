package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Feiitann extends BaseRole {

    private double totalDamageReceived = 0;
    private int currentTier = 0;
    private boolean powerActivated = false;
    private boolean fireAspectActive = false;

    public Feiitann(Player player, Main plugin) {
        super(player, 2, plugin, "Feiitann\n\n" +
                "Vous avez l'effet Speed 1 permanent.\n\n" +
                "Vous gagnez avec l'équipe 2.");
        applyPermanentSpeedEffect();
        applyNightStrength();
        startDayResistanceCheck();
    }

    private void applyPermanentSpeedEffect() {
        Player player = getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false)); // Speed 1
    }

    @Override
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
                break;
            case 3:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 0, false, false)); // +2 hearts total
                break;
            case 4:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 1, false, false)); // +4 hearts total
                applyNightStrength();
                applyFireAspect();
                break;
            case 5:
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 1, false, false)); // +4 hearts total
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, false, false)); // Permanent Strength 1
                applyFireAspect();
                break;
        }
    }

    private void applyNightStrength() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (player.getWorld().getTime() >= 13000 && player.getWorld().getTime() <= 23000) { // Vérifier si c'est la nuit
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 160, 0, false, false)); // Night Strength 1 pour 8 secondes
                } else {
                    player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                }
            }
        }.runTaskTimer(plugin, 0L, 80L); // Vérifier toutes les 4 secondes
    }

    private void startDayResistanceCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (powerActivated && currentTier == 5 && (player.getWorld().getTime() < 13000 || player.getWorld().getTime() > 23000)) { // Vérifier si c'est le jour et au palier 5
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 0, false, false)); // Day Resistance pour 8 secondes
                } else {
                    player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                }
            }
        }.runTaskTimer(plugin, 0L, 80L); // Vérifier toutes les 4 secondes
    }

    private void applyFireAspect() {
        fireAspectActive = true;
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (player.equals(getPlayer())) {
                // Appliquer une augmentation de 15% des dégâts
                double originalDamage = event.getDamage();
                double increasedDamage = originalDamage * 1.15;
                event.setDamage(increasedDamage);

                // Appliquer l'effet de Fire Aspect
                if (fireAspectActive && event.getEntity() instanceof Player) {
                    Player target = (Player) event.getEntity();
                    target.setFireTicks(100); // Enflamme l'adversaire pendant 5 secondes
                }
            }
        }
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        // No special items for Feiitann
    }
}
