package fr.nathan.plugin.commands;

import fr.nathan.plugin.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StopCommand implements CommandExecutor {

    private final Main plugin;

    public StopCommand(Main plugin) {
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

        plugin.getUhcTimer().stop(); // Arrêter le timer UHC
        plugin.stopGame();
        return true;
    }
}
