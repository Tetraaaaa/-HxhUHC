package fr.nathan.plugin.roles;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
        super(player, 2, plugin, "Uvogin\nVous possedez l'effet force 1\n\nItems\nNen: Déclenche une explosion au prochain coup qui infligera 3 coeurs de dégâts supplémentaire à tout les joueurs dans un rayon de 10 blocks, les membres de la brigarde phanom sont immunisés (15minutes de cooldown)\nBerserk: Vous octroie l'effet resistance 1 et augmente votre force 5% pendant 2 minutes (15 minutes de cooldown)\n\nVous gagnez avec la Brigade Phantom");
        giveBerserkItem();
        giveNenItem();
        startStrengthEffectTask();
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

    private void startStrengthEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getPlayer().isOnline()) {
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 150, 0, false, false)); // Strength 1 for 5 seconds
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Refresh every 2 seconds (40 ticks)
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
        applyBerserkEffect(player);
        berserkCooldowns.put(playerId, currentTime);

        new BukkitRunnable() {
            @Override
            public void run() {
                deactivateBerserk();
            }
        }.runTaskLater(plugin, BERSERK_DURATION); // 2 minutes later
    }

    private void applyBerserkEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) BERSERK_DURATION, 0, false, false)); // Increase Strength by 5%
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) BERSERK_DURATION, 0, false, false)); // Resistance 1
    }

    private void deactivateBerserk() {
        isBerserkActive = false;
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
        }.runTaskLater(plugin, 600L); // Nen active for 30 seconds
    }

    @Override
    public void handleInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null) {
            if (item.getType() == Material.NETHER_STAR && item.getItemMeta().getDisplayName().equals("Berserk")) {
                activateBerserk();
                event.setCancelled(true);
            } else if (item.getType() == Material.BLAZE_POWDER && item.getItemMeta().getDisplayName().equals("Nen")) {
                activateNen();
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isNenActive && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();

            if (damager.equals(getPlayer())) {
                isNenActive = false;
                createNenExplosion(event.getEntity().getLocation());
            }
        }
    }

    private void createNenExplosion(Location location) {
        location.getWorld().playEffect(location, Effect.EXPLOSION_LARGE, 0); // Create a visual explosion

        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 10, 10, 10);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (player.equals(getPlayer())) {
                    continue; // Skip Uvogin
                }
                if (plugin.getCustomPlayer(player).getTeam() != 2) {
                    player.damage(6); // 3 hearts of damage
                }
            }
        }
    }
}
