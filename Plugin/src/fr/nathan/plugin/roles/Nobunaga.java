package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Nobunaga extends BaseRole {

    private static final int NEN_COOLDOWN = 120000 * 5; // 10 minutes in ticks
    private static final int MAX_NEN_USES = 3;
    private static final int NEN_DURATION_TICKS = 2 * 60 * 20; // 2 minutes in ticks
    private boolean nenActive = false;
    private Location nenCenter;
    private int nenUses = 0;
    private int nenDurationCounter = 0;
    private static final Map<UUID, Long> nenCooldowns = new HashMap<>();

    public Nobunaga(Player player, Main plugin) {
        super(player, 2, plugin, "Nobunaga\n\n" +
                "Vous avez l'effet Force 1 de nuit.\n\n" +
                "Vous gagnez avec l'équipe 2.");
        applyNightStrength();
        giveDiamondSword();
        giveNenItem();
    }

    private void applyNightStrength() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (!nenActive && player.getWorld().getTime() >= 13000 && player.getWorld().getTime() <= 23000) { // Vérifier si c'est la nuit
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 160, 0, false, false)); // Night Strength 1 pour 8 secondes
                } else if (!nenActive) {
                    player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); 
    }

    private void giveDiamondSword() {
        Player player = getPlayer();
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 4);
        player.getInventory().addItem(sword);
    }

    private void giveNenItem() {
        Player player = getPlayer();
        ItemStack nen = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta meta = nen.getItemMeta();
        meta.setDisplayName("Nen");
        nen.setItemMeta(meta);
        player.getInventory().addItem(nen);
    }

    public void activateNen() {
        Player player = getPlayer();
        UUID playerId = player.getUniqueId();

        // Vérifier le cooldown et le nombre d'utilisations
        long currentTime = System.currentTimeMillis();
        if (nenUses >= MAX_NEN_USES || (nenCooldowns.containsKey(playerId) && (currentTime - nenCooldowns.get(playerId)) < NEN_COOLDOWN)) {
            long timeLeft = NEN_COOLDOWN - (currentTime - nenCooldowns.get(playerId));
            player.sendMessage("Vous devez attendre " + (timeLeft / 1000) + " secondes avant de pouvoir utiliser cette capacité à nouveau.");
            return;
        }

        // Activer le Nen
        nenActive = true;
        nenCenter = player.getLocation();
        nenUses++;
        nenCooldowns.put(playerId, currentTime);
        nenDurationCounter = NEN_DURATION_TICKS; // Initialiser le compteur à 120 (2 minutes en ticks)

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!nenActive) {
                    cancel();
                    return;
                }

                if (!player.isOnline()) {
                    deactivateNen();
                    cancel();
                    return;
                }

                if (player.getLocation().distance(nenCenter) > 10) {
                    deactivateNen();
                    cancel();
                    return;
                }

                if (nenDurationCounter <= 0) {
                    deactivateNen();
                    cancel();
                    return;
                }

                applyNenEffect(player);
                showNenParticles();
                nenDurationCounter--;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Check every second
    }

    private void applyNenEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 1, false, false)); // Force 2 (30% de force supplémentaire) pour 2 secondes, réappliqué chaque seconde
    }

    private void deactivateNen() {
        nenActive = false;
        Player player = getPlayer();
        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        applyNightStrength(); // Réappliquer la force de nuit si applicable
        player.sendMessage("La zone Nen est désactivée.");
        // Mettre à jour le cooldown
        nenCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void showNenParticles() {
        double radius = 10.0;
        for (int angle = 0; angle < 360; angle += 10) {
            double radians = Math.toRadians(angle);
            double x = nenCenter.getX() + radius * Math.cos(radians);
            double z = nenCenter.getZ() + radius * Math.sin(radians);
            Location particleLocation = new Location(nenCenter.getWorld(), x, nenCenter.getY(), z);
            nenCenter.getWorld().playEffect(particleLocation, Effect.MOBSPAWNER_FLAMES, 0);
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Aucun traitement spécial pour les dégâts infligés
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.BLAZE_POWDER && item.getItemMeta().getDisplayName().equals("Nen")) {
            activateNen();
            event.setCancelled(true); // Annule l'événement pour éviter d'autres actions
        }
    }
}
