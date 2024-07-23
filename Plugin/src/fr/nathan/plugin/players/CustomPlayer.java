package fr.nathan.plugin.players;

import org.bukkit.entity.Player;

public class CustomPlayer {

    protected final Player player;
    private int team;

    public CustomPlayer(Player player, int team) {
        this.player = player;
        this.team = team;
    }

    public Player getPlayer() {
        return player;
    }
    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }
}
