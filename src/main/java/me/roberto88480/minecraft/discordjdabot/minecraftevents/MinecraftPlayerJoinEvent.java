package me.roberto88480.minecraft.discordjdabot.minecraftevents;

import me.roberto88480.minecraft.discordjdabot.DiscordMinecraftPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinecraftPlayerJoinEvent implements Listener {
    DiscordMinecraftPlugin discordMinecraftPlugin;
    public MinecraftPlayerJoinEvent(DiscordMinecraftPlugin discordMinecraftPlugin) {
        this.discordMinecraftPlugin = discordMinecraftPlugin;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent event){
        discordMinecraftPlugin.updateActivity();
    }
}
