package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Leolio extends BaseRole implements Listener {

    private static final long GOLDEN_APPLE_INTERVAL = 20 * 60 * 20L; // 20 minutes en ticks
    private static final long NEN_COOLDOWN = 300000;
    private static final int NEN_HIT_COUNT = 3;
    private boolean nenActive = false;
    private int hitsRemaining = 0;
    private static final Map<UUID, Long> nenCooldowns = new HashMap<>();

    public Leolio(Player player, Main plugin) {
        super(player, 1, plugin, "Leolio\n\n" +
                "Vous ne possédez aucun effet particulier.\n" +
                "Vous obtenez 3 pommes en or à l'annonce des rôles et toutes les 20 minutes\n\n" +
                "Items\n" +
                "Nen: Vous permet d'augmenter votre portée d'attaque de 3 blocks à 5 blocks (5 minutes de cooldown)\n\n" +
                "Vous gagnez avec les Hunters.");
        giveNenItem();
        startGoldenAppleTask();
    }

    private void giveNenItem() {
        ItemStack nen = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = nen.getItemMeta();
        meta.setDisplayName("Nen");
        nen.setItemMeta(meta);
        getPlayer().getInventory().addItem(nen);
    }

    private void startGoldenAppleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getPlayer().isOnline()) {
                    getPlayer().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 3));
                    getPlayer().sendMessage("Vous avez reçu 3 pommes d'or.");
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, GOLDEN_APPLE_INTERVAL);
    }

    public void activateNen() {
        Player player = getPlayer();
        UUID playerId = player.getUniqueId();

        long currentTime = System.currentTimeMillis();
        if (nenCooldowns.containsKey(playerId)) {
            long lastUseTime = nenCooldowns.get(playerId);
            long timeLeft = (lastUseTime + NEN_COOLDOWN) - currentTime;

            if (timeLeft > 0) {
                player.sendMessage("Vous devez attendre " + (timeLeft / 1000) + " secondes avant de pouvoir utiliser cette capacité à nouveau.");
                return;
            }
        }

        nenActive = true;
        hitsRemaining = NEN_HIT_COUNT;
        nenCooldowns.put(playerId, currentTime);
        player.sendMessage("Nen activé ! Vous avez " + NEN_HIT_COUNT + " coups avec une portée augmentée.");

        new BukkitRunnable() {
            @Override
            public void run() {
                nenActive = false;
                hitsRemaining = 0;
                player.sendMessage("Nen désactivé !");
            }
        }.runTaskLater(plugin, 200L); // Nen active for 10 seconds
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.BLAZE_POWDER && item.getItemMeta().getDisplayName().equals("Nen")) {
            activateNen();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (nenActive && hitsRemaining > 0) {
            Player player = event.getPlayer();
            if (player.equals(getPlayer())) {
                checkForExtendedRangeAttack(player);
            }
        }
    }

    private void checkForExtendedRangeAttack(Player player) {
        Vector direction = player.getEyeLocation().getDirection();
        Location location = player.getEyeLocation().clone();

        for (double i = 0; i < 5; i += 0.5) {
            location.add(direction.clone().multiply(0.5));
            List<Entity> nearbyEntities = player.getWorld().getEntities();
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player) {
                    if (entity.getLocation().distance(location) < 1.5) {
                        // Infliger des dégâts et appliquer une vélocité
                        applyDamage(player, (LivingEntity) entity);
                        Vector knockback = direction.clone().multiply(0.5).setY(0.2); // Ajouter une composante vers le haut
                        entity.setVelocity(knockback);
                        hitsRemaining--;
                        if (hitsRemaining <= 0) {
                            nenActive = false;
                            player.sendMessage("Nen désactivé !");
                        }
                        return;
                    }
                }
            }
        }
    }

    private void applyDamage(Player attacker, LivingEntity target) {
        double damage = 4.0; // 2 coeurs de dégâts

        // Vérifier les effets de résistance
        for (PotionEffect effect : target.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
                if (effect.getAmplifier() == 0) { // Résistance 1
                    damage = 3.0; // 1.5 coeurs de dégâts
                } else if (effect.getAmplifier() == 1) { // Résistance 2
                    damage = 2.0; // 1 coeur de dégâts
                }
            }
        }

        // Appliquer les dégâts
        target.damage(damage);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (nenActive && hitsRemaining > 0 && event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (player.equals(getPlayer())) {
                event.setCancelled(true); // Annuler l'événement de dommage pour gérer manuellement les coups
            }
        }
    }
}
