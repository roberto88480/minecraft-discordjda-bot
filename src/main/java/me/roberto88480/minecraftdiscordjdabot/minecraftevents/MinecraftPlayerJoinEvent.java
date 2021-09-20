package me.roberto88480.minecraftdiscordjdabot.minecraftevents;

import me.roberto88480.minecraftdiscordjdabot.DiscordMinecraftPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MinecraftPlayerJoinEvent implements Listener {
    DiscordMinecraftPlugin discordMinecraftPlugin;
    public MinecraftPlayerJoinEvent(DiscordMinecraftPlugin discordMinecraftPlugin) {
        this.discordMinecraftPlugin = discordMinecraftPlugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        discordMinecraftPlugin.updateActivity();
        //System.out.println("Player JOIN");
    }
}
