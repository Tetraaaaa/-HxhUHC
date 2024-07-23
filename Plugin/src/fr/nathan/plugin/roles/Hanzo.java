package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
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
        startSpeedEffectTask();
    }

    private void giveFumigeneItem() {
        ItemStack fumigene = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = fumigene.getItemMeta();
        meta.setDisplayName("Fumigène");
        fumigene.setItemMeta(meta);
        getPlayer().getInventory().addItem(fumigene);
    }

    public void applySpeedEffect() {
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false), true);
    }

    private void startSpeedEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getPlayer().isOnline()) {
                    applySpeedEffect();
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 60L); // Exécuter toutes les 60 ticks (3 secondes)
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
    public void handleInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.NETHER_STAR && item.getItemMeta().getDisplayName().equals("Fumigène")) {
            activateInvisibility();
            event.setCancelled(true); // Annule l'événement pour éviter d'autres actions
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isInvisible) {
            deactivateInvisibility();
        }
    }
}
