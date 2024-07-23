package fr.nathan.plugin.commands;

import fr.nathan.plugin.Main;
import fr.nathan.plugin.roles.BaseRole;
import fr.nathan.plugin.roles.Hanzo;
import fr.nathan.plugin.roles.Leolio;
import fr.nathan.plugin.roles.Nobunaga;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RoleCommand implements CommandExecutor {

    private final Main plugin;

    public RoleCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /role add <player> <role>");
            return false;
        }

        String action = args[0];
        String playerName = args[1];
        String roleName = args[2];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage("Player not found");
            return false;
        }

        if ("add".equalsIgnoreCase(action)) {
            BaseRole role = null;
            switch (roleName.toLowerCase()) {
                case "hanzo":
                    role = new Hanzo(player, plugin);
                    break;
                case "leolio":
                    role = new Leolio(player, plugin);
                    break;
                case "nobunaga":
                    role = new Nobunaga(player, plugin);
                    break;
                // Ajoutez d'autres rôles ici si nécessaire
                default:
                    sender.sendMessage("Role not found");
                    return false;
            }
            plugin.getRoleManager().assignRole(player, role);
            sender.sendMessage("Role " + roleName + " assigned to player " + playerName);
        } else {
            sender.sendMessage("Invalid action. Use add.");
        }

        return true;
    }
}
