package fr.nathan.plugin.commands;

import fr.nathan.plugin.Main;
import fr.nathan.plugin.players.CustomPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {

    private final Main plugin;

    public TeamCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent exécuter cette commande.");
            return false;
        }

        Player player = (Player) sender;
        CustomPlayer customPlayer = plugin.getCustomPlayer(player);

        if (customPlayer == null) {
            player.sendMessage("Vous n'avez pas de classe personnalisée.");
            return false;
        }

        player.sendMessage("Vous êtes dans l'équipe " + customPlayer.getTeam());
        return true;
    }
}
