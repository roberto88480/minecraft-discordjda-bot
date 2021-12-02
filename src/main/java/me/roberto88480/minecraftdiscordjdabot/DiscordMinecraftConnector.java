package me.roberto88480.minecraftdiscordjdabot;

import me.roberto88480.minecraftusernameuuidconverter.UsernameToUUIDConverter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiscordMinecraftConnector extends ListenerAdapter {
    private final JDA jda;
    private final Logger logger;
    private final Plugin plugin;

    public DiscordMinecraftConnector(@NotNull String token, @NotNull Plugin plugin) throws LoginException {
        this.logger = plugin.getLogger();
        this.plugin = plugin;
        // We don't need any intents for this bot. Slash commands work without any intents!
        jda = JDABuilder.createLight(token, Collections.emptyList())
                .addEventListeners(this)
                .setActivity(Activity.playing(String.format("Minecraft %d/%d", 0, plugin.getServer().getMaxPlayers())))
                .build();

        CommandData minecraftCommand = new CommandData("minecraft", "Show Minecraft server info");
        CommandData whitelistCommand = new CommandData("whitelist", "Show whitelisted players or add a player")
                .addOption(OptionType.STRING, "playername", "Add this player to the Minecraft whitelist", false);

        try {
            jda.awaitReady();
            List<Command> commands = jda.retrieveCommands().submit().get();
            //Delete Global Commands exept "minecraft"
            for (Command command : commands) {
                if (!command.getName().equals("minecraft")){
                    command.delete().queue();
                    logger.log(Level.INFO,"Deleted global Discord Command: " + command.getName());
                }
            }
            // Create global "minecraft" command if it does not exist
            if (commands.stream().anyMatch(c -> c.getName().equals("minecraft"))) {
                logger.log(Level.INFO,"Discord Command already exists: " + minecraftCommand.getName());
            } else {
                // This can take up to 1 hour to show up in the client
                jda.upsertCommand(minecraftCommand).queue();
                logger.log(Level.INFO,"Added global Discord Command: " + minecraftCommand.getName());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        //jda.upsertCommand(whitelistCommand).queue();

        for (Guild g : jda.getGuilds()) {
            logger.log(Level.INFO, "Registering Whitelist Command on Guild "+ g.getName() + "(" + g.getId() + ")");
            g.upsertCommand(whitelistCommand).queue();
        }
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
                        UUID playeruuid;
                        try {
                            playeruuid = UsernameToUUIDConverter.getUUID(playername);
                            Bukkit.getOfflinePlayer(playeruuid).setWhitelisted(true);
                            event.reply(String.format("Added player `%s` (UUID: %s) to whitelist!", playername, playeruuid)).queue();
                            Bukkit.broadcastMessage(String.format("[Discord] %s added %s (UUID: %s) to the whitelist", event.getUser().getAsTag(), playername, playeruuid));
                        } catch (IOException e) {
                            event.reply("Internal Server Error (Java IOException)").setEphemeral(true).queue();
                            logger.warning(String.format("IOException while %s tried to add %s to whitelist: %s", event.getUser().getAsTag(), playername, Arrays.toString(e.getStackTrace())));
                        } catch (ParseException e) {
                            event.reply(String.format("Internal Server Error (Java ParseException). Maybe %s is not a vaild username.", playername)).setEphemeral(true).queue();
                            logger.warning(String.format("ParseException while %s tried to add %s to whitelist: %s", event.getUser().getAsTag(), playername, Arrays.toString(e.getStackTrace())));
                        } catch (RuntimeException e) {
                            event.reply(String.format("Internal Server Error (Java RuntimeException). Maybe %s is not a vaild username.", playername)).setEphemeral(true).queue();
                            logger.warning(String.format("RuntimeException while %s tried to add %s to whitelist: %s", event.getUser().getAsTag(), playername, Arrays.toString(e.getStackTrace())));
                        }
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
