package me.roberto88480.minecraftdiscordjdabot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class DiscordMinecraftConnector extends ListenerAdapter {
    private final JDA jda;
    private final Plugin plugin;
    //private final Logger logger;
    public DiscordMinecraftConnector(String token) throws LoginException {
        this(token, 0);
    }

    public DiscordMinecraftConnector(String token, int maxPlayers) throws LoginException {
        this.plugin = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("RobertosMinecraftDiscordBot"));
        //this.logger = plugin.getLogger();
        // We don't need any intents for this bot. Slash commands work without any intents!
        jda = JDABuilder.createLight(token, Collections.emptyList())
                .addEventListeners(this)
                .setActivity(Activity.playing(String.format("Minecraft %d/%d", 0, maxPlayers)))
                .build();

        //jda.upsertCommand("ping", "Calculate ping of the bot").queue();
        // This can take up to 1 hour to show up in the client
        jda.upsertCommand(
                new CommandData("minecraft", "Show Minecraft server info")
        ).queue();
        jda.upsertCommand(
                new CommandData("whitelist", "Show whitelisted players or add a player")
                    .addOption(OptionType.STRING,"playername", "Add this player to the Minecraft whitelist", false)
        ).queue();
        //System.out.println(jda.retrieveCommands().submit().join().get(0).delete().submit().join().toString());
        //System.out.println(jda.retrieveCommands().submit().join().toString());
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event)
    {
        switch (event.getName()){
            case "minecraft" -> {
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                int maxPlayers = Bukkit.getMaxPlayers();
                if (onlinePlayers.size() > 0) {
                    event.reply(String.format("There are %d of a max of %d players online: %s", onlinePlayers.size(), maxPlayers, onlinePlayers.stream().map(Player::getName).collect(Collectors.joining(", ")))).setEphemeral(true).queue();
                } else {
                    event.reply(String.format("There are 0 of a max of %d players online", maxPlayers)).setEphemeral(true).queue();
                }
            }
            case "whitelist" -> {
                OptionMapping playerOption = event.getOption("playername");
                if (playerOption == null){
                    event.reply(String.format("There are %d whitelisted players: %s", Bukkit.getWhitelistedPlayers().size(), Bukkit.getWhitelistedPlayers().stream().map(OfflinePlayer::getName).collect(Collectors.joining(", ")))).setEphemeral(true).queue();
                } else {
                    String playername = playerOption.getAsString();
                    if (playername.matches("^\\w{3,16}$")) {
                        /*
                        try {
                            Callable<Boolean> task = () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("whitelist add %s", playername));
                            if (Bukkit.getScheduler().callSyncMethod(
                                    this.plugin,
                                    task
                            ).get()) {
                                event.reply(String.format("Added player `%s` to whitelist!", playername)).queue();
                                Bukkit.broadcastMessage(String.format("Discord User %s added %s to the whitelist", event.getUser().getAsTag(), playername));
                            } else {
                                event.reply(String.format("An error occured and %s could not be added to the whitelist.", playername)).queue();
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            event.reply(String.format("An error occured and %s could not be added to the whitelist.", playername)).queue();
                        }
                         */
                        Bukkit.getOfflinePlayer(playername).setWhitelisted(true);
                        event.reply(String.format("Added player `%s` to whitelist!", playername)).queue();
                        Bukkit.broadcastMessage(String.format("[Discord] %s added %s to the whitelist", event.getUser().getAsTag(), playername));
                    } else {
                        event.reply(String.format("Invalid playername `%s`", playername)).setEphemeral(true).queue();
                    }
                }
            }
            default -> event.reply("Not yet implemented :(").setEphemeral(true).queue();
        }
    }
    public void shutdown(){
        jda.shutdown();
    }
    public void setActivityPlayingMinecraft(int online, int maxplayers){
        jda.getPresence().setActivity(Activity.playing(String.format("Minecraft %d/%d", online, maxplayers)));
    }
}
