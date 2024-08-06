package fr.nathan.plugin.commands;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class StartCommand implements CommandExecutor {

    private final Main plugin;

    public StartCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!plugin.getAdmins().contains(player)) {
                player.sendMessage("Vous n'avez pas la permission d'utiliser cette commande.");
                return false;
            }
        }

        // Démarrer le compte à rebours
        new BukkitRunnable() {
            int countdown = 10;

            @Override
            public void run() {
                if (countdown > 0) {
                    Bukkit.broadcastMessage("Démarrage dans " + countdown + " secondes !");
                    countdown--;
                } else {
                    Bukkit.broadcastMessage("La partie commence !");
                    teleportPlayersRandomly();
                    plugin.enableDamageAfterDelay(20 * 60); // Activer les dégâts après 1 minute
                    plugin.getUhcTimer().start(); // Assurez-vous que UhcTimer a une méthode start()
                    World world = plugin.getUhcWorld();
                    world.setTime(0); // Remettre le temps à zéro
                    plugin.setPvp(world, true); // Activer le PvP
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }

    private void teleportPlayersRandomly() {
        World world = plugin.getUhcWorld();
        Random random = new Random();
        int radius = 500; // Rayon de la zone de jeu, moitié du diamètre défini par la bordure

        for (Player player : Bukkit.getOnlinePlayers()) {
            int x = random.nextInt(radius * 2) - radius;
            int z = random.nextInt(radius * 2) - radius;
            int y = world.getHighestBlockYAt(x, z) + 1;
            player.teleport(new Location(world, x, y, z));
        }
    }
}
