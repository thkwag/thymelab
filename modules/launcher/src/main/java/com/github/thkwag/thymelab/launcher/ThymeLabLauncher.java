package com.github.thkwag.thymelab.launcher;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.thkwag.thymelab.launcher.config.ConfigManager;
import com.github.thkwag.thymelab.launcher.config.LocaleManager;
import com.github.thkwag.thymelab.launcher.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class ThymeLabLauncher {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        
        SwingUtilities.invokeLater(() -> {
            ConfigManager config = new ConfigManager("thymelab-launcher.properties");
            config.load();
            LocaleManager localeManager = new LocaleManager(config.getProperty("language", "en"));
            
            MainFrame mainFrame = new MainFrame(config, localeManager);
            mainFrame.loadWindowState();
            mainFrame.setVisible(true);
            mainFrame.startProcess();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                mainFrame.saveWindowState();
                config.save();
            }));

            // Set application icon
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(ThymeLabLauncher.class.getResource("/icon.png")));
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                mainFrame.setIconImage(icon.getImage());
            }
        });
    }
}
