package me.roberto88480.minecraftdiscordjdabot;

import me.roberto88480.minecraftusernameuuidconverter.UsernameToUUIDConverter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;
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

        CommandData minecraftCommandData = new CommandData("minecraft", "Show Minecraft server info");
            CommandData whitelistCommandData = new CommandData("whitelist", "Show whitelisted players or add a player")
                .addOption(OptionType.STRING, "playername", "Add this player to the Minecraft whitelist", false)
                .setDefaultEnabled(false);

        try {
            jda.awaitReady();
            jda.updateCommands().addCommands(minecraftCommandData).addCommands(whitelistCommandData).queue();
            List<Command> commands = jda.retrieveCommands().complete();
            Optional<Command> whitelistCommand = commands.stream().filter(c -> c.getName().equals("whitelist")).findAny();
            if (whitelistCommand.isPresent()){
                HashMap<Guild, List<CommandPrivilege>> gr = new HashMap<>();
                LinkedList<CommandPrivilege> userCommandPrivileges = new LinkedList<>();
                for (Long roleId : plugin.getConfig().getLongList("discord.slashcommands.whitelist.allowedroles")) {
                    Role role = jda.getRoleById(roleId);
                    if (role != null) {
                        List<CommandPrivilege> a = gr.getOrDefault(role.getGuild(), new LinkedList<>());
                        a.add(CommandPrivilege.enable(role));
                        gr.put(role.getGuild(), a);
                    }
                }
                for (Long userId : plugin.getConfig().getLongList("discord.slashcommands.whitelist.allowedusers")) {
                    User user = jda.retrieveUserById(userId).complete();
                    if (user != null)
                        userCommandPrivileges.add(CommandPrivilege.enable(user));
                }
                whitelistCommand.ifPresent(w -> {
                    for (Guild g : jda.getGuilds()) {
                        List<CommandPrivilege> l = gr.getOrDefault(g, new LinkedList<>());
                        l.addAll(userCommandPrivileges);
                        w.updatePrivileges(g, l).queue();
                    }
                });
            }
            jda.getGuilds().forEach(g -> g.retrieveCommands().complete().forEach(c -> c.delete().queue()));
        } catch (InterruptedException e) {
            e.printStackTrace();
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
                if (
                        plugin.getConfig().getBoolean("discord.slashcommands.usechannelwhitelist") &&
                                !plugin.getConfig().getStringList("discord.slashcommands.whitelistedchannels").contains(event.getChannel().getId())
                ){
                    event.reply("This command is not allowed here.").setEphemeral(true).queue();
                } else {
                    OptionMapping playerOption = event.getOption("playername");
                    if (playerOption == null) {
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
