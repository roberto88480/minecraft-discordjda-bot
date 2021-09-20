package me.roberto88480.minecraftdiscordjdabot.minecraftevents;

import me.roberto88480.minecraftdiscordjdabot.DiscordMinecraftPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MinecraftPlayerQuitEvent implements Listener {
    DiscordMinecraftPlugin discordMinecraftPlugin;
    public MinecraftPlayerQuitEvent(DiscordMinecraftPlugin discordMinecraftPlugin) {
        this.discordMinecraftPlugin = discordMinecraftPlugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        discordMinecraftPlugin.updateActivity(true);
        //System.out.println("Player QUIT");
    }
}
