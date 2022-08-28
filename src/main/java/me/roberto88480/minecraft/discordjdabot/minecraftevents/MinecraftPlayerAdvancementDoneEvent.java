package me.roberto88480.minecraft.discordjdabot.minecraftevents;

import me.roberto88480.minecraft.discordjdabot.DiscordMinecraftPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;

public class MinecraftPlayerAdvancementDoneEvent implements Listener {
    DiscordMinecraftPlugin discordMinecraftPlugin;
    public MinecraftPlayerAdvancementDoneEvent(DiscordMinecraftPlugin discordMinecraftPlugin) {
        this.discordMinecraftPlugin = discordMinecraftPlugin;
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerAdvancementDoneEvent(@NotNull PlayerAdvancementDoneEvent event){
        discordMinecraftPlugin.announceAdvancement(event.getPlayer(), event.getAdvancement());
    }
}
