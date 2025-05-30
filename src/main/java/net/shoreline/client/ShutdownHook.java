package net.shoreline.client;

import net.shoreline.client.api.file.ClientConfiguration;
import net.shoreline.client.impl.module.client.IRCModule;
import net.shoreline.client.init.Modules;

/**
 * @author h_ypi
 * @since 1.0
 */
public class ShutdownHook extends Thread {
    /**
     *
     */
    public ShutdownHook() {
        setName("OvaqPlus-SHk");
    }

    /**
     * This runs when the game is shutdown and saves the
     * {@link ClientConfiguration} files.
     *
     * @see ClientConfiguration#saveClient()
     */
    @Override
    public void run() {
        OvaqPlus.info("Config Saveing…");
        OvaqPlus.CONFIG.saveClient();
        OvaqPlus.info("PRC stoping…");
        OvaqPlus.RPC.stopRPC();

        if (Modules.IRC.isEnabled() && IRCModule.chat.isConnected()) {
            IRCModule.chat.disconnect();
        }
    }
}
