package fr.nathan.plugin;

import fr.nathan.plugin.commands.*;
import fr.nathan.plugin.players.CustomPlayer;
import fr.nathan.plugin.players.Spectator;
import fr.nathan.plugin.roles.BaseRole;
import fr.nathan.plugin.roles.RoleManager;
import fr.nathan.plugin.utils.UhcTimer;
import fr.nathan.plugin.utils.DayNightCycle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private World uhcWorld;
    private UhcTimer uhcTimer;
    private DayNightCycle dayNightCycle;
    private boolean damageEnabled = false;
    private final Map<Player, CustomPlayer> customPlayers = new HashMap<>();
    private final Map<Player, Spectator> spectators = new HashMap<>();
    private final Set<Integer> assignedTeams = new HashSet<>();
    private final Set<Player> admins = new HashSet<>();
    private RoleManager roleManager;
    private WinCondition winCondition;

    @Override
    public void onEnable() {
        instance = this;
        System.out.println("ON");

        // Charger la configuration
        saveDefaultConfig();

        // Initialiser le chronomètre UHC
        uhcTimer = new UhcTimer(this);

        // Initialiser les conditions de victoire
        winCondition = new WinCondition(this);

        // Récupérer le monde par défaut
        uhcWorld = Bukkit.getWorlds().get(0);

        // Initialiser le cycle jour/nuit
        dayNightCycle = new DayNightCycle(this, uhcWorld);

        // Désactiver le PvP
        setPvp(uhcWorld, true);

        // Désactiver les dégâts
        damageEnabled = true;

        // Générer la plateforme
        generatePlatform(uhcWorld, 0, 100, 0);

        // Téléporter les joueurs sur la plateforme
        teleportPlayers(uhcWorld, 0, 101, 0);

        // Désactiver la régénération naturelle des cœurs
        disableNaturalRegeneration(uhcWorld);

        // Définir les bordures de la carte
        setWorldBorder(uhcWorld, 1000);

        // Démarrer le cycle jour/nuit modifié
        dayNightCycle.start();

        // Initialiser le gestionnaire de rôles
        roleManager = new RoleManager(this);

        // Enregistrer les commandes
        this.getCommand("setseed").setExecutor(new SetSeedCommand(this));
        this.getCommand("admin").setExecutor(new AdminCommand(this));
        this.getCommand("start").setExecutor(new StartCommand(this));
        this.getCommand("stop").setExecutor(new StopCommand(this));
        this.getCommand("classe").setExecutor(new ClassCommand(this));
        this.getCommand("team").setExecutor(new TeamCommand(this));
        this.getCommand("hxh").setExecutor(new HxHCommand(this)); // Enregistrer la nouvelle commande

        // Enregistrer les événements
        Bukkit.getPluginManager().registerEvents(this, this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Supprimer tous les effets de potion actifs
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            // Réinitialiser la santé maximale à la valeur par défaut (20.0)
            player.setMaxHealth(20.0);
            player.setHealth(player.getMaxHealth()); // Réinitialiser la santé actuelle
        }

    }

    @Override
    public void onDisable() {
        System.out.println("OFF");
        savePlayerTeams();
    }

    public static Main getInstance() {
        return instance;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = getConfig();
        String uuid = player.getUniqueId().toString();

        if (config.contains("players." + uuid + ".team")) {
            int team = config.getInt("players." + uuid + ".team");
            customPlayers.put(player, new CustomPlayer(player, team));
            Bukkit.getLogger().log(Level.INFO, "Player " + player.getName() + " rejoined and assigned to team " + team);
        } else {
            int team = getNextAvailableTeam();
            customPlayers.put(player, new CustomPlayer(player, team));
            config.set("players." + uuid + ".team", team);
            saveConfig();
            Bukkit.getLogger().log(Level.INFO, "Player " + player.getName() + " assigned to team " + team);
        }

        player.sendMessage("Bienvenue " + player.getName() + " dans l'équipe " + customPlayers.get(player).getTeam() + "!");

        // Réassigner le scoreboard du timer au joueur lorsqu'il rejoint
        player.setScoreboard(uhcTimer.getScoreboard());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        CustomPlayer customPlayer = customPlayers.get(player);
        if (customPlayer != null) {
            customPlayers.remove(player);
            spectators.put(player, new Spectator(player));
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            World world = getUhcWorld();
            Location spawnLocation = world.getSpawnLocation();
            player.teleport(spawnLocation);
            winCondition.checkWinCondition();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CustomPlayer customPlayer = customPlayers.remove(player);
        if (customPlayer != null) {
            winCondition.checkWinCondition();
        }
    }

    public void assignRole(Player player, BaseRole role) {
        roleManager.assignRole(player, role);
    }

    public int getNextAvailableTeam() {
        int team = 1;
        while (assignedTeams.contains(team)) {
            team++;
        }
        assignedTeams.add(team);
        return team;
    }

    public Map<Player, CustomPlayer> getCustomPlayers() {
        return customPlayers;
    }

    public Set<Player> getAdmins() {
        return admins;
    }

    public void stopGame() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(new Location(uhcWorld, 0, 101, 0));
        }
        Bukkit.broadcastMessage("La partie est terminée !");
    }

    public void generatePlatform(World world, int centerX, int centerY, int centerZ) {
        int halfSize = 15;

        for (int x = centerX - halfSize; x <= centerX + halfSize; x++) {
            for (int z = centerZ - halfSize; x <= centerX + halfSize; x++) {
                world.getBlockAt(x, centerY, z).setType(Material.BARRIER);
            }
        }

        int barrierHeight = 5;
        for (int x = centerX - halfSize; x <= centerX + halfSize; x++) {
            for (int y = 1; y <= barrierHeight; y++) {
                world.getBlockAt(x, centerY + y, centerZ - halfSize).setType(Material.BARRIER);
                world.getBlockAt(x, centerY + y, centerZ + halfSize).setType(Material.BARRIER);
            }
        }
        for (int z = centerZ - halfSize; z <= centerZ + halfSize; z++) {
            for (int y = 1; y <= barrierHeight; y++) {
                world.getBlockAt(centerX - halfSize, centerY + y, z).setType(Material.BARRIER);
                world.getBlockAt(centerX + halfSize, centerY + y, z).setType(Material.BARRIER);
            }
        }
    }

    public void teleportPlayers(World world, int x, int y, int z) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(new Location(world, x, y, z));
            player.setScoreboard(uhcTimer.getScoreboard());
        }
    }

    public void disableNaturalRegeneration(World world) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule naturalRegeneration false");
    }

    public void setWorldBorder(World world, int size) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(size);
    }

    public void setPvp(World world, boolean enabled) {
        world.setPVP(enabled);
    }

    public void enableDamageAfterDelay(long delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                damageEnabled = true;
                Bukkit.broadcastMessage("Les dégâts sont maintenant activés !");
            }
        }.runTaskLater(Main.getInstance(), delay);
    }

    public World getUhcWorld() {
        return uhcWorld;
    }

    public void setUhcWorld(World uhcWorld) {
        this.uhcWorld = uhcWorld;
        Bukkit.getLogger().info("UHC World set to: " + uhcWorld.getName());
        setPvp(uhcWorld, false);
        disableNaturalRegeneration(uhcWorld);
        generatePlatform(uhcWorld, 0, 100, 0);
        teleportPlayers(uhcWorld, 0, 101, 0);
        setWorldBorder(uhcWorld, 1000);
    }

    public UhcTimer getUhcTimer() {
        return uhcTimer;
    }

    public DayNightCycle getDayNightCycle() {
        return dayNightCycle;
    }

    public CustomPlayer getCustomPlayer(Player player) {
        return customPlayers.get(player);
    }

    public Spectator getSpectator(Player player) {
        return spectators.get(player);
    }

    public void resetPlayerClasses() {
        customPlayers.clear();
        spectators.clear();
        assignedTeams.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            FileConfiguration config = getConfig();
            String uuid = player.getUniqueId().toString();
            int team;
            if (config.contains("players." + uuid + ".team")) {
                team = config.getInt("players." + uuid + ".team");
            } else {
                team = getNextAvailableTeam();
                config.set("players." + uuid + ".team", team);
            }
            CustomPlayer customPlayer = new CustomPlayer(player, team);
            customPlayers.put(player, customPlayer);
            Bukkit.getLogger().log(Level.INFO, "Player " + player.getName() + " assigned to team " + team);
        }
        saveConfig();
    }

    public void savePlayerTeams() {
        FileConfiguration config = getConfig();
        for (Map.Entry<Player, CustomPlayer> entry : customPlayers.entrySet()) {
            Player player = entry.getKey();
            CustomPlayer customPlayer = entry.getValue();
            config.set("players." + player.getUniqueId().toString() + ".team", customPlayer.getTeam());
        }
        saveConfig();
    }

    public boolean isDamageEnabled() {
        return damageEnabled;
    }
}
