package com.github.thkwag.thymelab.launcher.ui;

import com.github.thkwag.thymelab.launcher.config.ConfigManager;
import com.github.thkwag.thymelab.launcher.process.AppProcessManager;
import com.github.thkwag.thymelab.launcher.ui.components.*;
import com.github.thkwag.thymelab.launcher.ui.dialogs.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainForm extends JFrame {
    private final JPanel mainPanel;
    private final LogPanel logPanel;
    private final ControlPanel controlPanel;
    private final MainMenuBar menuBar;
    private final ConfigManager config;
    private AppProcessManager processManager;

    public MainForm(ConfigManager config) {
        this.config = config;
        
        mainPanel = new JPanel(new BorderLayout(0, 1));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        logPanel = new LogPanel();
        controlPanel = new ControlPanel(config);
        ResourceBundle bundle = ResourceBundle.getBundle("messages", 
            Locale.forLanguageTag(config.getProperty("language", "en")));
        menuBar = new MainMenuBar(this, bundle);
        
        layoutComponents();
        initializeListeners();

        addShutdownHook();
        setUncaughtExceptionHandler();

        // Initialize UI components
        initComponents();
        
        // Add WindowListener to initialize functionality after window is fully displayed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    initializeFunctionality();
                });
            }
        });
    }

    private void layoutComponents() {
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(logPanel, BorderLayout.CENTER);
    }

    private void initializeListeners() {
        // ... Initialize listeners
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // process.destroy();
        }));
    }

    private void setUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Unhandled exception caught: " + throwable.getMessage());
            throwable.printStackTrace();
            // Exit program on exception
            System.exit(1);
        });
    }

    public MainMenuBar getMainMenuBar() {
        return menuBar;
    }

    public void startProcess() {
        if (processManager != null) {
            processManager.startProcess();
        }
    }

    public void stopProcess() {
        if (processManager != null) {
            processManager.stopProcess();
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void appendLog(String line) {
        logPanel.appendLog(line);
    }

    public void setMaxBufferSize(int size) {
        logPanel.setMaxBufferSize(size);
    }

    public void setLogBufferText(String text) {
        controlPanel.getLogBufferField().setText(text);
    }

    public void setLogLevelComboItem(String item) {
        controlPanel.getLogLevelCombo().setSelectedItem(item);
    }

    public void setLanguageComboItem(String item) {
        controlPanel.getLanguageCombo().setSelectedItem(item);
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public JMenuItem getProgramSettingsMenuItem() {
        return menuBar.getProgramSettingsMenuItem();
    }

    public JMenuItem getThymeleafSettingsMenuItem() {
        return menuBar.getThymeleafSettingsMenuItem();
    }

    public JMenuItem getAboutMenuItem() {
        return menuBar.getAboutMenuItem();
    }

    public void clearLog() {
        logPanel.clearLog();
    }

    public void setSelectedFont(String font) {
        controlPanel.setSelectedFont(font);
    }

    public void setSelectedFontSize(int size) {
        controlPanel.setSelectedFontSize(size);
    }

    public String getSelectedFont() {
        return controlPanel.getSelectedFont();
    }

    public int getSelectedFontSize() {
        return controlPanel.getSelectedFontSize();
    }

    public void updateLogFont(String fontFamily, int fontSize) {
        logPanel.updateLogFont(fontFamily, fontSize);
    }

    public void showSettingsDialog(MainFrame parent) {
        // Get current language setting
        Locale currentLocale = Locale.forLanguageTag(config.getProperty("language", "en"));
        ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);

        SettingsDialog dialog = new SettingsDialog(parent, config, bundle);
        dialog.setVisible(true);
    }

    public void showThymeleafSettingsDialog(Frame parent, ResourceBundle bundle, ConfigManager config) {
        // Use existing bundle
        ThymeleafSettingsDialog dialog = new ThymeleafSettingsDialog(parent, bundle, config);
        dialog.setVisible(true);
    }

    public void showAboutDialog(MainFrame parent) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", 
            Locale.forLanguageTag(config.getProperty("language", "en")));
        AboutDialog dialog = new AboutDialog(parent, bundle, config);
        dialog.setVisible(true);
    }

    private void initComponents() {
        // Existing UI component initialization code
        // ... 
    }

    private void initializeFunctionality() {
        // Move functionality initialization code here
        // e.g.: Process manager init, settings load, etc.
        processManager = new AppProcessManager(
            message -> logPanel.appendLog(message),
            () -> controlPanel.onProcessStopped(),
            config
        );

        // Set button event handlers
        controlPanel.getStartButton().addActionListener(e -> startProcess());
        controlPanel.getStopButton().addActionListener(e -> stopProcess());
    
    }
} 