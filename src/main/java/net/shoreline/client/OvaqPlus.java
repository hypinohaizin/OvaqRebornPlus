package net.shoreline.client;

import net.shoreline.client.api.Identifiable;
import net.shoreline.client.api.event.handler.EventBus;
import net.shoreline.client.api.event.handler.EventHandler;
import net.shoreline.client.api.file.ClientConfiguration;
import net.shoreline.client.api.render.shader.ShadersPool;
import net.shoreline.client.impl.manager.client.DiscordManager;
import net.shoreline.client.impl.module.client.IRCModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.BaritoneChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OvaqPlus {
    public static Logger LOGGER;
    public static EventHandler EVENT_HANDLER;
    public static ClientConfiguration CONFIG;
    public static DiscordManager RPC;
    public static ShutdownHook SHUTDOWN;
    public static Executor EXECUTOR;

    public static void logAsciiArt() {
        String asciiArt =
                "  ___                   ____      _                      \n" +
                        " / _ \\__   ____ _  __ _|  _ \\ ___| |__   ___  _ __ _ __  \n" +
                        "| | | \\ \\ / / _` |/ _` | |_) / _ \\ '_ \\ / _ \\| '__| '_ \\ \n" +
                        "| |_| |\\ V / (_| | (_| |  _ <  __/ |_) | (_) | |  | | | |\n" +
                        " \\___/  \\_/ \\__,_|\\__, |_| \\_\\___|_.__/ \\___/|_|  |_| |_|\n" +
                        "                   |___/                                \n";

        LOGGER.info(asciiArt);
    }

    public static void init() {
        LOGGER = LogManager.getLogger("OvaqPlus");
        logAsciiArt();

       // info("OvaqPlus Loading...");
        BaritoneChecker.checkBaritone();
        info("Baritone Checking...");
        info("preInit starting ...");
        EXECUTOR = Executors.newFixedThreadPool(1);
        EVENT_HANDLER = new EventBus();
        info("init starting ...");
        Managers.init();
        Modules.init();
        ShadersPool.initShaders();
        RPC = new DiscordManager();
        DiscordManager.startRPC();
        info("discordrpc starting ...");
        info("postInit starting ...");
        CONFIG = new ClientConfiguration();
        Managers.postInit();
        SHUTDOWN = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(SHUTDOWN);
        info("Config loading ...");
        CONFIG.loadClient();

        if (Modules.IRC.isEnabled() && !IRCModule.chat.isConnected()) {
            try {
                InetAddress address = InetAddress.getByName("www.google.com");
                boolean isReachable = address.isReachable(5000);
                if (isReachable) {
                    info("Connecting to IRC Server (Init)");
                    IRCModule.chat.connect();
                } else {
                    info("Network Connection Error. (Maybe Bug");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        info("OvaqPlus Load is done.");
    }

    public static void info(String message) {
        LOGGER.info(String.format("[OvaqPlus] %s", message));
    }

    public static void info(String message, Object... params) {
        LOGGER.info(String.format("[OvaqPlus] %s", message), params);
    }

    public static void info(Identifiable feature, String message) {
        LOGGER.info(String.format("[%s] %s", feature.getId(), message));
    }

    public static void info(Identifiable feature, String message, Object... params) {
        LOGGER.info(String.format("[%s] %s", feature.getId(), message), params);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Object... params) {
        LOGGER.error(String.format(message, params));
    }

    public static void error(Identifiable feature, String message) {
        LOGGER.error(String.format("[%s] %s", feature.getId(), message));
    }

    public static void error(Identifiable feature, String message, Object... params) {
        LOGGER.error(String.format("[%s] %s", feature.getId(), message), params);
    }
}
