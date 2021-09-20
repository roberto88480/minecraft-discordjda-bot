package me.roberto88480.minecraftdiscordjdabot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class DiscordBotMain extends ListenerAdapter
{
    public static void main(String[] args) throws LoginException
    {
        // TODO: Maybe replace everything with exit(1)
        //
        if (args.length < 1) {
            System.out.println("You have to provide a token as first argument!");
            System.exit(1);
        }
        DiscordMinecraftConnector discordMinecraftConnector = new DiscordMinecraftConnector(args[0]);
    }
}