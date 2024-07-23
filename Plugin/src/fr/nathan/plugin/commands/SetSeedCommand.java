package fr.nathan.plugin.commands;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetSeedCommand implements CommandExecutor {

    private final Main plugin;

    public SetSeedCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Usage: /setseed <seed>");
            return false;
        }

        long seed;
        try {
            seed = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage("La seed doit être un nombre.");
            return false;
        }

        World world = Bukkit.createWorld(new WorldCreator("uhc_world").seed(seed));
        plugin.setUhcWorld(world);
        sender.sendMessage("Nouveau monde généré avec la seed: " + seed);

        return true;
    }
}
