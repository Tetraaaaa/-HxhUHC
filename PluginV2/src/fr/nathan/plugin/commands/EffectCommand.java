package fr.nathan.plugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cette commande ne peut être utilisée que par un joueur.");
            return false;
        }

        Player player = (Player) sender;
        double strengthBonus = getEffectAmplifier(player, PotionEffectType.INCREASE_DAMAGE) * 15.0;
        double speedBonus = getEffectAmplifier(player, PotionEffectType.SPEED) * 20.0;
        double resistanceBonus = getEffectAmplifier(player, PotionEffectType.DAMAGE_RESISTANCE) * 20.0;

        player.sendMessage("Effets supplémentaires:");
        player.sendMessage("Force: +" + strengthBonus + "%");
        player.sendMessage("Speed: +" + speedBonus + "%");
        player.sendMessage("Résistance: +" + resistanceBonus + "%");

        return true;
    }

    private int getEffectAmplifier(Player player, PotionEffectType effectType) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(effectType)) {
                return effect.getAmplifier() + 1; 
            }
        }
        return 0;
    }
}
