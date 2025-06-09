package net.shoreline.client.util;

import net.fabricmc.loader.api.FabricLoader;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class BaritoneChecker {

    public static void checkBaritone() {
        boolean isBaritoneLoaded = FabricLoader.getInstance().isModLoaded("baritone");
        String BARITONE_DOWNLOAD_URL = "https://ovaqclient.web.fc2.com/baritone-api.jar";

        if (!isBaritoneLoaded) {
            JFrame frame = new JFrame();
            frame.setAlwaysOnTop(true);

            JOptionPane optionPane = new JOptionPane(
                    "Baritone is not installed.\nThis client require Baritone",
                    JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null
            );

            JButton downloadButton = new JButton("Download Baritone");
            downloadButton.addActionListener(e -> {
                try {
                    Desktop.getDesktop().browse(new URI(BARITONE_DOWNLOAD_URL));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> frame.dispose());

            optionPane.setOptions(new Object[]{downloadButton, closeButton});

            JDialog dialog = new JDialog(frame, "Baritone Checker", true);
            dialog.setContentPane(optionPane);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            throw new IllegalStateException("Baritone is not loaded.");
        }
    }
}
