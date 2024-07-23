package fr.nathan.plugin;

import fr.nathan.plugin.players.CustomPlayer;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class WinCondition {

    private final Main plugin;

    public WinCondition(Main plugin) {
        this.plugin = plugin;
    }

    public void checkWinCondition() {
        Set<Integer> remainingTeams = new HashSet<>();
        for (CustomPlayer customPlayer : plugin.getCustomPlayers().values()) {
            remainingTeams.add(customPlayer.getTeam());
        }

        if (remainingTeams.size() == 1) {
            int winningTeam = remainingTeams.iterator().next();
            Bukkit.broadcastMessage("Félicitations à l'équipe " + winningTeam + " pour avoir gagné !");
            plugin.stopGame();
        }
    }
}
