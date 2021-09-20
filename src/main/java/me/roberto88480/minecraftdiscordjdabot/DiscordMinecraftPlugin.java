package me.roberto88480.minecraftdiscordjdabot;

import me.roberto88480.minecraftdiscordjdabot.minecraftevents.MinecraftPlayerJoinEvent;
import me.roberto88480.minecraftdiscordjdabot.minecraftevents.MinecraftPlayerQuitEvent;
import org.bukkit.Bukkit;
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
        assert token != null;
        assert token.length()>20;
        try {
            discordMinecraftConnector = new DiscordMinecraftConnector(token, getServer().getMaxPlayers());
        } catch (LoginException ignored) {

        }
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new MinecraftPlayerJoinEvent(this), this);
        pm.registerEvents(new MinecraftPlayerQuitEvent(this), this);
    }
     //@Override
     public void onDisable(){
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
}