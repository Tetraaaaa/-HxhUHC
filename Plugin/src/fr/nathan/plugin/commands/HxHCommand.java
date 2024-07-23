package fr.nathan.plugin.commands;

import fr.nathan.plugin.Main;
import fr.nathan.plugin.roles.BaseRole;
import fr.nathan.plugin.roles.Kirua;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HxHCommand implements CommandExecutor {

    private final Main plugin;

    public HxHCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent exécuter cette commande.");
            return false;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("Usage: /hxh <command>");
            return false;
        }

        String subCommand = args[0];
        BaseRole role = plugin.getRoleManager().getRole(player);

        if (role instanceof Kirua && subCommand.equalsIgnoreCase("invi")) {
            ((Kirua) role).applyInvisibility();
            player.sendMessage("Invisibilité activée.");
            return true;
        } else {
            player.sendMessage("Vous n'avez pas la permission d'utiliser cette commande ou elle est inconnue.");
            return false;
        }
    }
}
