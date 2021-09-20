package me.roberto88480.minecraftdiscordjdabot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.List;

public class DiscordMinecraftConnector extends ListenerAdapter {
    private JDA jda;
    private DiscordMinecraftConnector() {
    }
    public DiscordMinecraftConnector(String token) throws LoginException {
        this(token, 0);
    }

    public DiscordMinecraftConnector(String token, int maxPlayers) throws LoginException {
        // We don't need any intents for this bot. Slash commands work without any intents!
        jda = JDABuilder.createLight(token, Collections.emptyList())
                .addEventListeners(new DiscordMinecraftConnector())
                .setActivity(Activity.playing(String.format("Minecraft %d/%d", 0, maxPlayers)))
                .build();

        //jda.upsertCommand("ping", "Calculate ping of the bot").queue();
        // This can take up to 1 hour to show up in the client
        CommandData commandData = new CommandData("minecraft", "Info über den Minecraft Server");

        SubcommandData subcommandData_say = new SubcommandData("say", "Sende eine Nachricht an alle auf dem Server");
        subcommandData_say.addOption(OptionType.STRING, "Text", "Deine Nachricht");

        SubcommandData subcommandData_whitelist = new SubcommandData("whitelist", "Die Server-Whitelist verwalten");

        SubcommandData subcommandData_whitelist_add = new SubcommandData("add", "Minecraft Spieler zur Whitelist hinzufügen");
        subcommandData_whitelist_add.addOption(OptionType.STRING, "Spieler", "Minecraft Spielername", true);

        CommandCreateAction commandCreateAction = jda.upsertCommand(commandData);

        //System.out.println(jda.retrieveCommands().submit().join().get(0).delete().submit().join().toString());
        //System.out.println(jda.retrieveCommands().submit().join().toString());
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event)
    {
        if (!event.getName().equals("ping")) return; // make sure we handle the right command
        long time = System.currentTimeMillis();
        event.reply("Pong!").setEphemeral(true) // reply or acknowledge
                .flatMap(v ->
                        event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time) // then edit original
                ).queue(); // Queue both reply and edit
    }
    public void shutdown(){
        jda.shutdown();
    }
    public void setActivityPlayingMinecraft(int online, int maxplayers){
        String activityText = String.format("Minecraft %d/%d", online, maxplayers);
        System.out.println("Update Discord Activity: "+activityText);
        jda.getPresence().setActivity(Activity.playing(activityText));
    }
}
