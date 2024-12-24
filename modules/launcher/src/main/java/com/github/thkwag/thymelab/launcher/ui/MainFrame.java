package com.github.thkwag.thymelab.launcher.ui;

import com.github.thkwag.thymelab.launcher.config.ConfigManager;
import com.github.thkwag.thymelab.launcher.config.LocaleManager;
import com.github.thkwag.thymelab.launcher.process.AppProcessManager;
import com.github.thkwag.thymelab.launcher.ui.components.ControlPanel;
import com.github.thkwag.thymelab.launcher.ui.components.MainMenuBar;
import com.github.thkwag.thymelab.launcher.ui.dialogs.AboutDialog;
import com.github.thkwag.thymelab.launcher.ui.components.LogPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.imageio.ImageIO;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public class MainFrame extends JFrame {
    private final ConfigManager config;
    private final LocaleManager localeManager;
    private ResourceBundle bundle;

    private final AppProcessManager appProcessManager;
    private final MainForm mainForm;
    private ControlPanel controlPanel;
    private final MainMenuBar menuBar;

    private TrayIcon trayIcon;
    private MenuItem showHideMenuItem;
    private MenuItem exitMenuItem;
    private boolean isRunning = false;

    public MainFrame(ConfigManager config, LocaleManager localeManager) {
        this.config = config;
        this.localeManager = localeManager;
        this.bundle = localeManager.getBundle();

        mainForm = new MainForm(config);
        menuBar = mainForm.getMainMenuBar();
        setJMenuBar(menuBar);
        setContentPane(mainForm.getMainPanel());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setupIcon();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (SystemTray.isSupported()) {
                    SystemTray.getSystemTray().remove(trayIcon);
                }
                saveWindowState();
                System.exit(0);
            }

            @Override
            public void windowIconified(WindowEvent e) {
                if (SystemTray.isSupported()) {
                    setVisible(false);
                }
            }
        });

        // Set title
        String title = String.format("%s - %s", 
            bundle.getString("app_title"), 
            bundle.getString("sub_title"));
        setTitle(title);

        initActions();

        // Load settings
        int bufSize = config.getInt("log.buffer.size", 1000);
        mainForm.setMaxBufferSize(bufSize);
        mainForm.setLogBufferText(String.valueOf(bufSize));

        String savedLevel = config.getProperty("log.level", "INFO");
        mainForm.setLogLevelComboItem(savedLevel);

        String savedLang = config.getProperty("language", "en");
        mainForm.setLanguageComboItem(savedLang.equalsIgnoreCase("ko") ? "KO" : "EN");

        // Initialize process manager
        appProcessManager = new AppProcessManager(
                mainForm::appendLog,
            () -> SwingUtilities.invokeLater(() -> updateButtonStates(false)),
            config
        );

        // Initialize UI state
        updateButtonStates(true);
        updateTexts();

        // Load and apply font settings
        String fontFamily = config.getProperty("font.family", LogPanel.getDefaultMonospacedFont());
        int fontSize = config.getInt("font.size", 12);
        mainForm.updateLogFont(fontFamily, fontSize);

        setupTrayIcon();

        // Start process after window is shown
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> startApp());
            }
        });
    }

    private void initActions() {
        controlPanel = mainForm.getControlPanel();
        controlPanel.getStartButton().addActionListener(e -> startApp());
        controlPanel.getStopButton().addActionListener(e -> stopApp());
        controlPanel.getClearLogButton().addActionListener(e -> mainForm.clearLog());

        controlPanel.getLogLevelCombo().addActionListener(e -> {
            String level = (String) controlPanel.getLogLevelCombo().getSelectedItem();
            if (level != null) {
                config.setProperty("log.level", level);
            }
        });

        mainForm.getProgramSettingsMenuItem().addActionListener(e -> {
            mainForm.showSettingsDialog(this);
            
            String newLang = config.getProperty("language", "en");
            if (!newLang.equals(localeManager.getCurrentLanguage())) {
                localeManager.setLanguage(newLang);
                bundle = localeManager.getBundle();
                updateTexts();
            }
            
            int newBufferSize = config.getInt("log.buffer.size", 1000);
            mainForm.setMaxBufferSize(newBufferSize);
            mainForm.setLogBufferText(String.valueOf(newBufferSize));
            
            String newFont = config.getProperty("font.family", Font.SANS_SERIF);
            int newSize = config.getInt("font.size", 12);
            mainForm.updateLogFont(newFont, newSize);
        });

        mainForm.getThymeleafSettingsMenuItem().addActionListener(e -> {
            mainForm.showThymeleafSettingsDialog(this, bundle, config);
        });

        mainForm.getAboutMenuItem().addActionListener(e -> {
            showAboutDialog(this);
        });

        controlPanel.getLanguageCombo().addActionListener(e -> {
            String lang = (String) controlPanel.getLanguageCombo().getSelectedItem();
            if (lang != null) {
                String code = lang.equals("KO") ? "ko" : "en";
                localeManager.setLanguage(code);
                config.setProperty("language", code);
                bundle = localeManager.getBundle();
                updateTexts();
            }
        });
    }

    private void startApp() {
        if (!appProcessManager.isRunning()) {
            appProcessManager.startProcess();
            updateButtonStates(true);
            onProcessStart();
        }
    }

    private void stopApp() {
        if (appProcessManager.isRunning()) {
            appProcessManager.stopProcess();
            updateButtonStates(false);
            onProcessStop();
        }
    }

    private void updateButtonStates(boolean isRunning) {
        controlPanel.getStartButton().setEnabled(!isRunning);
        controlPanel.getStopButton().setEnabled(isRunning);
    }

    public void updateTexts() {
        controlPanel.getStartButton().setText(bundle.getString("start"));
        controlPanel.getStopButton().setText(bundle.getString("stop"));
        controlPanel.getClearLogButton().setText(bundle.getString("clear_log"));

        controlPanel.getLogLevelLabel().setText(bundle.getString("log_level"));
        controlPanel.getLogBufferLabel().setText(bundle.getString("log_buffer_size"));
        controlPanel.getLanguageLabel().setText(bundle.getString("language"));
        
        MainMenuBar mainMenuBar = (MainMenuBar) menuBar;
        mainMenuBar.getToolsMenu().setText(bundle.getString("menu_tools"));
        mainMenuBar.getHelpMenu().setText(bundle.getString("menu_help"));
        mainMenuBar.getProgramSettingsMenuItem().setText(bundle.getString("menu_program_settings"));
        mainMenuBar.getThymeleafSettingsMenuItem().setText(bundle.getString("menu_thymeleaf_settings"));
        mainMenuBar.getExitMenuItem().setText(bundle.getString("menu_exit"));
        mainMenuBar.getAboutMenuItem().setText(bundle.getString("menu_about"));
        
        controlPanel.getLogBufferUnit().setText(bundle.getString("lines"));
        controlPanel.setFontSettingsText(bundle.getString("font"), bundle.getString("font_size"));
        
        // Update title
        String title = String.format("%s - %s", 
            bundle.getString("app_title"), 
            bundle.getString("sub_title"));
        setTitle(title);

        // Update tray icon text and menu
        if (trayIcon != null) {
            trayIcon.setToolTip(getTooltipText());
            if (showHideMenuItem != null) {
                showHideMenuItem.setLabel(isVisible() ? bundle.getString("hide") : bundle.getString("show"));
            }
            if (exitMenuItem != null) {
                exitMenuItem.setLabel(bundle.getString("menu_exit"));
            }
        }
    }

    public void loadWindowState() {
        int width = config.getInt("window.width", 1024);
        int height = config.getInt("window.height", 768);
        int x = config.getInt("window.x", 200);
        int y = config.getInt("window.y", 150);
        int state = config.getInt("window.state", Frame.NORMAL);

        // Set screen size
        setSize(width, height);
        
        // Check all screen areas
        Rectangle virtualBounds = new Rectangle();
        for (GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            virtualBounds = virtualBounds.union(screen.getDefaultConfiguration().getBounds());
        }

        // Verify if saved position is valid
        if (!virtualBounds.contains(x, y)) {
            // Position at screen center
            x = (int) (virtualBounds.getCenterX() - (double) width / 2);
            y = (int) (virtualBounds.getCenterY() - (double) height / 2);
        }

        setLocation(x, y);
        setExtendedState(state);
    }

    public void saveWindowState() {
        config.setInt("window.width", getWidth());
        config.setInt("window.height", getHeight());
        config.setInt("window.x", getX());
        config.setInt("window.y", getY());
        config.setInt("window.state", getExtendedState());
    }

    public void startProcess() {
        mainForm.startProcess();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (showHideMenuItem != null) {
            showHideMenuItem.setLabel(visible ? bundle.getString("hide") : bundle.getString("show"));
        }
    }

    public void updateFromSettings() {
        String newLang = config.getProperty("language", "en");
        if (!newLang.equals(localeManager.getCurrentLanguage())) {
            localeManager.setLanguage(newLang);
            bundle = localeManager.getBundle();
            updateTexts();
        }
        
        int newBufferSize = config.getInt("log.buffer.size", 1000);
        mainForm.setMaxBufferSize(newBufferSize);
        mainForm.setLogBufferText(String.valueOf(newBufferSize));
        
        String newFont = config.getProperty("font.family", Font.SANS_SERIF);
        int newSize = config.getInt("font.size", 12);
        mainForm.updateLogFont(newFont, newSize);
    }

    private void onProcessStart() {
        isRunning = true;
        updateTrayIcon();
    }

    private void onProcessStop() {
        isRunning = false;
        updateTrayIcon();
    }

    private void setupTrayIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();

            // Show/Hide menu item
            showHideMenuItem = new MenuItem(bundle.getString("show"));
            showHideMenuItem.addActionListener(e -> toggleVisibility());
            popup.add(showHideMenuItem);

            // Add separator
            popup.addSeparator();

            // Exit menu item
            exitMenuItem = new MenuItem(bundle.getString("menu_exit"));
            exitMenuItem.addActionListener(e -> {
                if (SystemTray.isSupported()) {
                    tray.remove(trayIcon);
                }
                saveWindowState();
                System.exit(0);
            });
            popup.add(exitMenuItem);

            try {
                // Try multiple icon paths
                Image image = null;
                String[] iconPaths = {"/icon.png", "/images/icon.png", "/icons/icon.png"};
                
                for (String path : iconPaths) {
                    try {
                        image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(path)));
                        if (image != null) break;
                    } catch (Exception ignored) {}
                }
                
                // If no icon found, create a default one
                if (image == null) {
                    image = createDefaultIcon();
                }
                
                trayIcon = new TrayIcon(image, getTooltipText(), popup);
                trayIcon.setImageAutoSize(true);
                trayIcon.addActionListener(e -> toggleVisibility());
                
                try {
                    tray.add(trayIcon);
                } catch (AWTException e) {
                    System.err.println("Could not add system tray icon: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Could not setup system tray icon: " + e.getMessage());
            }
        }
    }

    private Image createDefaultIcon() {
        // Create a simple default icon (16x16 pixels)
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(0, 0, 16, 16);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, 15, 15);
        g2d.dispose();
        return image;
    }

    private void toggleVisibility() {
        if (isVisible()) {
            setVisible(false);
            showHideMenuItem.setLabel(bundle.getString("show"));
        } else {
            setVisible(true);
            setState(Frame.NORMAL);
            showHideMenuItem.setLabel(bundle.getString("hide"));
        }
    }

    private String getTooltipText() {
        String status = isRunning ? bundle.getString("running") : bundle.getString("stopped");
        return String.format("%s - %s", bundle.getString("app_title"), status);
    }

    private void updateTrayIcon() {
        if (trayIcon != null) {
            trayIcon.setToolTip(getTooltipText());
            showHideMenuItem.setLabel(isVisible() ? bundle.getString("hide") : bundle.getString("show"));
        }
    }

    private void setupIcon() {
        try {
            // Set platform-specific icon
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icon.icns"))));
            } else if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icon.ico"))));
            } else {
                setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public void showAboutDialog(MainFrame parent) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", 
            Locale.forLanguageTag(config.getProperty("language", "en")));
        AboutDialog dialog = new AboutDialog(parent, bundle, config);
        dialog.setVisible(true);
    }
} 