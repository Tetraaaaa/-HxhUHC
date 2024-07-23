package fr.nathan.plugin.players;

import org.bukkit.entity.Player;

public class Spectator {

    private final Player player;

    public Spectator(Player player) {
        this.player = player;
        setSpectatorMode();
    }

    private void setSpectatorMode() {
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
    }

    public Player getPlayer() {
        return player;
    }
}
