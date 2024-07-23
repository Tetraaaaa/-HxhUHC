package fr.nathan.plugin.commands;

import fr.nathan.plugin.Main;
import fr.nathan.plugin.players.CustomPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {

    private final Main plugin;

    public AdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /admin add/remove <player>");
            return false;
        }

        String action = args[0];
        String playerName = args[1];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage("Player not found");
            return false;
        }

        CustomPlayer customPlayer = plugin.getCustomPlayer(player);

        if ("add".equalsIgnoreCase(action)) {
            plugin.getAdmins().add(player);
            sender.sendMessage("Player " + playerName + " has been added as an admin.");
        } else if ("remove".equalsIgnoreCase(action)) {
            plugin.getAdmins().remove(player);
            sender.sendMessage("Player " + playerName + " has been removed as an admin.");
        } else {
            sender.sendMessage("Invalid action. Use add/remove.");
        }
        return true;
    }
}
