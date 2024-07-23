package fr.nathan.plugin.utils;

import fr.nathan.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class UhcTimer {

    private final Main plugin;
    private int taskId = -1; // Initialiser à une valeur invalide
    private Scoreboard scoreboard;
    private Objective objective;
    private int secondsElapsed;
    private int currentDay;

    public UhcTimer(Main plugin) {
        this.plugin = plugin;
        setupScoreboard();
    }

    private void setupScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("timer", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("UHC Timer");
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void start() {
        if (taskId != -1) {
            stop(); // Arrêter tout timer existant avant d'en démarrer un nouveau
        }

        secondsElapsed = 0;
        currentDay = 1;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                secondsElapsed++;
                int seconds = secondsElapsed % 60;
                int minutes = (secondsElapsed / 60) % 20; // chaque jour dure 20 minutes

                // Mettre à jour le timer dans le scoreboard
                updateScoreboard(minutes, seconds);

                if (secondsElapsed % 1200 == 0) { // 1200 secondes = 20 minutes
                    currentDay++;
                    Bukkit.broadcastMessage("Début du jour " + currentDay);
                }
            }
        }, 0L, 20L); // Répéter toutes les 20 ticks (1 seconde)
    }

    private void updateScoreboard(int minutes, int seconds) {
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        objective.getScore("Jour " + currentDay).setScore(2); // Score pour "Jour"
        objective.getScore("Temps : " + String.format("%02d:%02d", minutes, seconds)).setScore(1); // Score pour "Temps"
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1; // Réinitialiser le taskId
        }
    }
}
