package com.github.thkwag.thymelab.launcher.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Desktop;
import java.net.URI;
import com.github.thkwag.thymelab.launcher.config.ConfigManager;
import com.github.thkwag.thymelab.launcher.ui.dialogs.ActuatorInfoDialog;

public class ControlPanel extends JPanel {
    private JButton startButton;
    private JButton stopButton;
    private JComboBox<String> logLevelCombo;
    private JButton clearLogButton;
    private JLabel logLevelLabel;
    private JTextField logBufferField;
    private JComboBox<String> languageCombo;
    private JLabel logBufferLabel;
    private JLabel languageLabel;
    private JLabel logBufferUnitLabel;
    private JLabel fontSettingsLabel;
    private String selectedFont;
    private int selectedFontSize;
    private JLabel urlLabel;
    private final ConfigManager config;
    private JPanel statusIndicator;
    private Timer blinkTimer;

    public ControlPanel(ConfigManager config) {
        this.config = config;
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        initializeComponents();
        layoutComponents();
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Left panel - Start/Stop buttons and URL
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(startButton);
        leftPanel.add(stopButton);
        leftPanel.add(Box.createHorizontalStrut(5));  // Spacing before URL
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(urlLabel);
        leftPanel.add(statusIndicator);
        
        // Right panel - Log level and clear button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.add(logLevelLabel);
        rightPanel.add(logLevelCombo);
        rightPanel.add(clearLogButton);
        
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private void initializeComponents() {
        startButton = new JButton();
        stopButton = new JButton();
        logLevelCombo = new JComboBox<>();
        clearLogButton = new JButton();
        logLevelLabel = new JLabel();
        logBufferLabel = new JLabel();
        languageLabel = new JLabel();
        logBufferUnitLabel = new JLabel();
        fontSettingsLabel = new JLabel();
        languageCombo = new JComboBox<>();
        logBufferField = new JTextField(5);
        
        logLevelCombo.removeAllItems();
        logLevelCombo.addItem("INFO");
        logLevelCombo.addItem("DEBUG");
        logLevelCombo.addItem("WARN");
        logLevelCombo.addItem("ERROR");
        
        languageCombo.addItem("EN");
        languageCombo.addItem("KO");
        
        // Initialize URL label
        urlLabel = new JLabel();
        urlLabel.setVisible(false);
        urlLabel.setForeground(new Color(0, 102, 204));
        urlLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        urlLabel.setFont(urlLabel.getFont().deriveFont(Font.BOLD));
        urlLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(urlLabel.getText()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        logLevelCombo.addActionListener(e -> {
            String level = (String) logLevelCombo.getSelectedItem();
            if (level != null && logLevelCombo.isEnabled()) {
                updateLogLevel(level);
            }
        });
        
        // Start in disabled state
        logLevelCombo.setEnabled(false);
        logLevelLabel.setEnabled(false);
        
        // Initialize status indicator
        statusIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(2, 4, getWidth() - 4, getHeight() - 4);
            }
        };
        statusIndicator.setPreferredSize(new Dimension(12, 12));
        statusIndicator.setOpaque(false);
        statusIndicator.setVisible(false);
        statusIndicator.setCursor(new Cursor(Cursor.HAND_CURSOR));
        statusIndicator.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (statusIndicator.isVisible() && logLevelCombo.isEnabled()) {
                    int port = config.getInt("server.port", 8080);
                    ActuatorInfoDialog dialog = new ActuatorInfoDialog(
                        (Frame) SwingUtilities.getWindowAncestor(ControlPanel.this), port);
                    dialog.setVisible(true);
                }
            }
        });
        
        // Setup blink timer
        blinkTimer = new Timer(500, e -> {
            if (statusIndicator.isVisible()) {
                statusIndicator.setBackground(
                    statusIndicator.getBackground().equals(new Color(255, 50, 50)) ? 
                    new Color(180, 0, 0) : new Color(255, 50, 50)
                );
                statusIndicator.repaint();
            }
        });
    }

    private void updateLogLevel(String level) {
        int port = config.getInt("server.port", 8080);
        URI uri = URI.create(String.format("http://localhost:%d/actuator/loggers/com.github.thkwag.thymelab", port));
        
        new Thread(() -> {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) 
                    uri.toURL().openConnection();
                
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = String.format("{\"configuredLevel\": \"%s\"}", level).getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                int responseCode = conn.getResponseCode();
                if (responseCode != 200 && responseCode != 204) {  // Treat 204 as success too
                    System.err.println("Failed to update log level: " + responseCode);
                }
                
            } catch (Exception ex) {
                System.err.println("Error updating log level: " + ex.getMessage());
            }
        }).start();
    }

    private void checkActuatorStatus() {
        int port = config.getInt("server.port", 8080);
        URI uri = URI.create(String.format("http://localhost:%d/actuator/health", port));
        
        new Thread(() -> {
            for (int i = 0; i < 30; i++) {  // Try for 30 seconds
                try {
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) 
                        uri.toURL().openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(1000);
                    conn.setReadTimeout(1000);
                    
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        SwingUtilities.invokeLater(() -> {
                            setLogControlsEnabled(true);
                            statusIndicator.setBackground(new Color(0, 180, 0));
                            blinkTimer.stop();
                        });
                        return;
                    }
                    // Response code is not 200
                    System.err.println("Actuator health check failed: HTTP " + responseCode);
                    
                } catch (java.net.ConnectException e) {
                    System.err.println("Server not yet ready: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error checking actuator status: " + e.getMessage());
                    e.printStackTrace();
                }
                
                try {
                    Thread.sleep(1000);  // Wait 1 second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            // Connection failed after 30 seconds
            System.err.println("Actuator health check timed out after 30 seconds");
            SwingUtilities.invokeLater(() -> {
                setLogControlsEnabled(false);
                statusIndicator.setBackground(new Color(255, 50, 50));  // Change to red
                blinkTimer.stop();
            });
        }).start();
    }

    public JButton getStartButton() { return startButton; }
    public JButton getStopButton() { return stopButton; }
    public JComboBox<String> getLogLevelCombo() { return logLevelCombo; }
    public JButton getClearLogButton() { return clearLogButton; }
    public JTextField getLogBufferField() {
        return logBufferField;
    }
    public JComboBox<String> getLanguageCombo() {
        return languageCombo;
    }
    public JLabel getLogLevelLabel() { return logLevelLabel; }
    public JLabel getLogBufferLabel() { return logBufferLabel; }
    public JLabel getLanguageLabel() { return languageLabel; }
    public JLabel getLogBufferUnit() { return logBufferUnitLabel; }
    public JLabel getFontSettingsText() { return fontSettingsLabel; }

    public void setSelectedFont(String font) {
        this.selectedFont = font;
    }

    public void setSelectedFontSize(int size) {
        this.selectedFontSize = size;
    }

    public String getSelectedFont() {
        return selectedFont;
    }

    public int getSelectedFontSize() {
        return selectedFontSize;
    }

    public void setFontSettingsText(String fontText, String sizeText) {
        fontSettingsLabel.setText(fontText + " / " + sizeText);
    }

    public void showServerUrl(boolean show) {
        if (show) {
            int port = config.getInt("server.port", 8080);
            urlLabel.setText("http://localhost:" + port);
        }
        urlLabel.setVisible(show);
    }

    public void setLogControlsEnabled(boolean enabled) {
        logLevelCombo.setEnabled(enabled);
        logLevelLabel.setEnabled(enabled);
        if (!enabled) {
            logLevelCombo.setToolTipText("Actuator is not available");
        } else {
            logLevelCombo.setToolTipText(null);
        }
    }

    public void onProcessStarted() {
        showServerUrl(true);
        setLogControlsEnabled(false);
        statusIndicator.setVisible(true);
        statusIndicator.setBackground(Color.RED);
        blinkTimer.start();
        checkActuatorStatus();
    }

    public void onProcessStopped() {
        showServerUrl(false);
        setLogControlsEnabled(false);
        statusIndicator.setVisible(false);
        blinkTimer.stop();
    }
} 