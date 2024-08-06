package fr.nathan.plugin;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Admin {

    private static Set<String> admins = new HashSet<>();

    public static void addAdmin(Player player) {
        admins.add(player.getName());
    }

    public static void removeAdmin(Player player) {
        admins.remove(player.getName());
    }

    public static boolean isAdmin(Player player) {
        return admins.contains(player.getName());
    }
}
