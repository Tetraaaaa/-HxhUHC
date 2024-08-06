package fr.nathan.plugin.utils;

import fr.nathan.plugin.Main;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class DayNightCycle {

    private final Main plugin;
    private final World world;

    public DayNightCycle(Main plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    public void start() {
        new BukkitRunnable() {
            boolean isDay = true;

            @Override
            public void run() {
                if (isDay) {
                    world.setTime(0); // Début du jour
                } else {
                    world.setTime(13000); // Début de la nuit
                }
                isDay = !isDay;
            }
        }.runTaskTimer(plugin, 0L, 6000L); // Exécuter toutes les 5 minutes (6000 ticks)
    }
}
