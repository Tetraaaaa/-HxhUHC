package fr.nathan.plugin.commands;

import fr.nathan.plugin.Main;
import fr.nathan.plugin.roles.BaseRole;
import fr.nathan.plugin.roles.Hanzo;
import fr.nathan.plugin.roles.Leolio;
import fr.nathan.plugin.roles.Nobunaga;
import fr.nathan.plugin.roles.Kirua;
import fr.nathan.plugin.roles.Hisoka; 
import fr.nathan.plugin.roles.Uvogin; // Assurez-vous d'importer la classe Uvogin
import fr.nathan.plugin.roles.Feiitann; // Assurez-vous d'importer la classe Feiitann
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassCommand implements CommandExecutor {

    private final Main plugin;

    public ClassCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /class add <player> <role>");
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
                case "kirua":
                    role = new Kirua(player, plugin);
                    break;
                case "hisoka":
                    role = new Hisoka(player, plugin);
                    break;
                case "uvogin": // Ajoutez le case pour Uvogin
                    role = new Uvogin(player, plugin);
                    break;
                case "feiitann": // Ajoutez le case pour Feiitann
                    role = new Feiitann(player, plugin);
                    break;
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
