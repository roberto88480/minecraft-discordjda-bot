package me.roberto88480.minecraftusernameuuidconverter;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UsernameToUUIDConverterTest {
    private static final String uuid = "9cf7cd2840604dbaaa65c8c62bda50cc";
    private static final String fulluuid = "9cf7cd28-4060-4dba-aa65-c8c62bda50cc";
    private static final String name = "Roberto88480";

    @Test
    void getUUID() {
        try {
            assertEquals(UUID.fromString(fulluuid), UsernameToUUIDConverter.getUUID(name));
        } catch (IOException | ParseException e) {
            fail(e);
        }
    }

    @Test
    void getName() {
        try {
            assertEquals(name, UsernameToUUIDConverter.getName(UUID.fromString(fulluuid)));
        } catch (IOException | ParseException e) {
            fail(e);
        }
    }

    @Test
    void insertDashUUID() {
        assertEquals(fulluuid, UsernameToUUIDConverter.insertDashUUID(uuid));
    }
}