package me.roberto88480.minecraft.usernameuuidconverter;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

public class UsernameToUUIDConverter {
    private final static String baseURLbyName = "https://api.mojang.com/users/profiles/minecraft/";
    private final static String baseURLbyUUID = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static @NotNull JSONObject getJsonFromURL(@NotNull URL url) throws IOException, ParseException, RuntimeException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        //Getting the response code
        int responsecode = conn.getResponseCode();
        if (responsecode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responsecode);
        } else {
            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();
            JSONParser parse = new JSONParser();
            return  (JSONObject) parse.parse(inline.toString());
        }
    }
    public static @NotNull UUID getUUID(@NotNull String playername) throws IOException, ParseException, RuntimeException {
        URL url = new URL(baseURLbyName + playername);
        return UUID.fromString(insertDashUUID(getJsonFromURL(url).get("id").toString()));
    }
    public static @NotNull String getName(@NotNull UUID uuid) throws IOException, ParseException, RuntimeException {
        URL url = new URL(baseURLbyUUID + uuid);
        return getJsonFromURL(url).get("name").toString();
    }
    public static @NotNull String insertDashUUID(@NotNull String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        for (int i = 8; i <= 23; i += 5) {
            sb.insert(i, "-");
        }
        return sb.toString();
    }
}
