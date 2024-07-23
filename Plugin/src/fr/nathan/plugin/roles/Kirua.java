package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import fr.nathan.plugin.utils.ActionBarUtil;
import fr.nathan.plugin.utils.VisibilityUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
        super(player, 2, plugin, "Kirua\n\n" +
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
        startBaseSpeedEffectTask();
    }

    private void applyPermanentEffects() {
        Player player = getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0, false, false));
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
                    // Reapply base speed effect
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
                }
            }.runTaskLater(plugin, 1200L); // Reset divineSpeedActive after 1 minute (1200 ticks)
        } else {
            player.sendMessage("Pas assez de points électriques.");
        }
    }

    private void startBaseSpeedEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = getPlayer();
                if (!divineSpeedActive) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 0, false, false)); // Speed 1 for 8 seconds, refreshed every second
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Check every second
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
                        player.sendMessage("Vous êtes redevenu visible.");
                        isInvisible = false;
                        cancel();
                        return;
                    }
                }

                if (player.getWorld().getTime() < 13000 || player.getWorld().getTime() > 23000) {
                    VisibilityUtil.setPlayerInvisible(player, false);
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
        }
    }

    private void applyKnockback(Player target) {
        Vector knockback = target.getVelocity().multiply(-0.1); // Apply a small knockback in the opposite direction
        target.setVelocity(knockback);
    }

    @Override
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
