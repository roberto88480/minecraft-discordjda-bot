package me.roberto88480.minecraftdiscordjdabot;

import me.roberto88480.minecraftdiscordjdabot.minecraftevents.MinecraftPlayerAdvancementDoneEvent;
import me.roberto88480.minecraftdiscordjdabot.minecraftevents.MinecraftPlayerJoinEvent;
import me.roberto88480.minecraftdiscordjdabot.minecraftevents.MinecraftPlayerQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

@SuppressWarnings("unused")
public class DiscordMinecraftPlugin extends JavaPlugin  {
    DiscordMinecraftConnector discordMinecraftConnector;
    @Override
    public void onEnable(){
        loadConfig();
        final String token = getConfig().getString("config.discord_bot_token");
        if (token == null || token.length()<20){
            this.getLogger().severe("Please specify a Discord-Bot-Token in the config.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            discordMinecraftConnector = new DiscordMinecraftConnector(token, getServer().getMaxPlayers(), this);
        } catch (LoginException e) {
            this.getLogger().severe(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new MinecraftPlayerJoinEvent(this), this);
        pm.registerEvents(new MinecraftPlayerQuitEvent(this), this);
        pm.registerEvents(new MinecraftPlayerAdvancementDoneEvent(this), this);
    }
     @Override
     public void onDisable(){
        if (discordMinecraftConnector != null )
            discordMinecraftConnector.shutdown();
     }
    private void loadConfig(){
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void updateActivity(){
        updateActivity(false);
    }
    public void updateActivity(boolean leaving) {
        discordMinecraftConnector.setActivityPlayingMinecraft(
                leaving?getServer().getOnlinePlayers().size()-1:getServer().getOnlinePlayers().size(),
                getServer().getMaxPlayers()
        );
    }
    public void announceAdvancement(Player p, Advancement a){

    }
}