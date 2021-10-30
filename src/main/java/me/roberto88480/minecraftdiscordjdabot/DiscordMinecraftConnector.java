package me.roberto88480.minecraftdiscordjdabot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiscordMinecraftConnector extends ListenerAdapter {
    private final JDA jda;
    private final Logger logger;
    public DiscordMinecraftConnector(String token) throws LoginException {
        this(token, 0);
    }

    public DiscordMinecraftConnector(String token, int maxPlayers) throws LoginException {
        this.logger = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("RobertosMinecraftDiscordBot")).getLogger();
        // We don't need any intents for this bot. Slash commands work without any intents!
        jda = JDABuilder.createLight(token, Collections.emptyList())
                .addEventListeners(this)
                .setActivity(Activity.playing(String.format("Minecraft %d/%d", 0, maxPlayers)))
                .build();

        //jda.upsertCommand("ping", "Calculate ping of the bot").queue();
        // This can take up to 1 hour to show up in the client
        jda.upsertCommand(
            new CommandData("minecraft", "Show Server Info")
                .addSubcommands(
                    new SubcommandData("say", "Send a message to every online player")
                        .addOption(OptionType.STRING, "message", "Your message", true),
                    new SubcommandData("list", "List online players"),
                    new SubcommandData("whitelist", "Show Server Whitelist"),
                    new SubcommandData("whitelistadd", "Add player to whitelist")
                        .addOption(OptionType.STRING, "player", "Minecraft Playername", true)
                )
        ).queue();
        //System.out.println(jda.retrieveCommands().submit().join().get(0).delete().submit().join().toString());
        //System.out.println(jda.retrieveCommands().submit().join().toString());
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event)
    {
        if (event.getName().equals("minecraft")) {
            switch (Objects.requireNonNull(event.getSubcommandName())) {
                case "say" -> {
                    logger.log(Level.INFO, "[Discord] " + event.getUser().getAsTag() + ": " + Objects.requireNonNull(event.getOption("message")).getAsString());
                    event.reply("Message sendt!").setEphemeral(true).queue();
                }
                case "whitelist" -> event.reply(String.format("Whitelisted Players (%d): %s", Bukkit.getWhitelistedPlayers().size(), Bukkit.getWhitelistedPlayers().stream().map(OfflinePlayer::getName).collect(Collectors.joining(", ")))).setEphemeral(true).queue();
                case "whitelistadd" -> {
                    String player = Objects.requireNonNull(event.getOption("player")).getAsString();
                    if (player.matches("^\\w{3,16}$")) {
                        event.reply(String.format("Added Player **%s** to Whitelist! (not really yet...)", player)).queue();
                    } else {
                        event.reply("Please specify a vaild playername!").setEphemeral(true).queue();
                    }
                }
                case "list" -> {
                    Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                    int maxPlayers = Bukkit.getMaxPlayers();
                    if (onlinePlayers.size() > 0) {
                        event.reply(String.format("Online Players (%d/%d)\n%s", onlinePlayers.size(), maxPlayers, onlinePlayers.stream().map(Player::getName).collect(Collectors.joining(", ")))).setEphemeral(true).queue();
                    } else {
                        event.reply(String.format("Online Players (0/%d)", maxPlayers)).setEphemeral(true).queue();
                    }
                }
                default -> event.reply("Not yet implemented :(").setEphemeral(true).queue();
            }
        }
    }
    public void shutdown(){
        jda.shutdown();
    }
    public void setActivityPlayingMinecraft(int online, int maxplayers){
        jda.getPresence().setActivity(Activity.playing(String.format("Minecraft %d/%d", online, maxplayers)));
    }
}
