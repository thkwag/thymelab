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
import java.util.ResourceBundle;

public class MainFrame extends JFrame {
    private final ConfigManager config;
    private final LocaleManager localeManager;
    private ResourceBundle bundle;

    private AppProcessManager appProcessManager;
    private MainForm mainForm;
    private ControlPanel controlPanel;
    private MainMenuBar menuBar;

    private TrayIcon trayIcon;

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
            line -> mainForm.appendLog(line), 
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

        // 창이 표시된 후에 프로세스 시작
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
        
        // 제목 업데이트 추가
        String title = String.format("%s - %s", 
            bundle.getString("app_title"), 
            bundle.getString("sub_title"));
        setTitle(title);
    }

    public void loadWindowState() {
        int width = config.getInt("window.width", 1024);
        int height = config.getInt("window.height", 768);
        int x = config.getInt("window.x", 200);
        int y = config.getInt("window.y", 150);
        int state = config.getInt("window.state", Frame.NORMAL);

        // 화면 크기 설정
        setSize(width, height);
        
        // 모든 화면의 영역을 확인
        Rectangle virtualBounds = new Rectangle();
        for (GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            virtualBounds = virtualBounds.union(screen.getDefaultConfiguration().getBounds());
        }

        // 저장된 위치가 유효한지 확인
        if (!virtualBounds.contains(x, y)) {
            // 화면 중앙에 위치킴
            x = (int) (virtualBounds.getCenterX() - width / 2);
            y = (int) (virtualBounds.getCenterY() - height / 2);
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
        mainForm.getControlPanel().getStartButton().setEnabled(false);
        mainForm.getControlPanel().getStopButton().setEnabled(true);
        mainForm.getControlPanel().onProcessStarted();
    }

    private void onProcessStop() {
        mainForm.getControlPanel().getStartButton().setEnabled(true);
        mainForm.getControlPanel().getStopButton().setEnabled(false);
        mainForm.getControlPanel().onProcessStopped();
    }

    private void setupTrayIcon() {
        if (!SystemTray.isSupported()) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            
            // Load tray icon image
            Image iconImage = ImageIO.read(getClass().getResourceAsStream("/icon.png"));
            int trayIconSize = (int) tray.getTrayIconSize().getHeight();
            Image scaledImage = iconImage.getScaledInstance(trayIconSize, trayIconSize, Image.SCALE_SMOOTH);
            
            // Create popup menu
            PopupMenu popup = new PopupMenu();
            
            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(e -> {
                setVisible(true);
                setExtendedState(JFrame.NORMAL);
            });
            popup.add(showItem);
            
            popup.addSeparator();
            
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                tray.remove(trayIcon);
                saveWindowState();
                System.exit(0);
            });
            popup.add(exitItem);
            
            // Create tray icon
            String trayTitle = String.format("%s - %s", 
                bundle.getString("app_title"), 
                bundle.getString("sub_title"));
            trayIcon = new TrayIcon(scaledImage, trayTitle, popup);
            trayIcon.setImageAutoSize(true);
            
            // Restore window on double click
            trayIcon.addActionListener(e -> {
                setVisible(true);
                setExtendedState(JFrame.NORMAL);
            });
            
            tray.add(trayIcon);
            
        } catch (Exception e) {
            e.printStackTrace();
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

    private void setupIcon() {
        try {
            // Set platform-specific icon
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                setIconImage(ImageIO.read(getClass().getResourceAsStream("/icon.icns")));
            } else if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                setIconImage(ImageIO.read(getClass().getResourceAsStream("/icon.ico")));
            } else {
                setIconImage(ImageIO.read(getClass().getResourceAsStream("/icon.png")));
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