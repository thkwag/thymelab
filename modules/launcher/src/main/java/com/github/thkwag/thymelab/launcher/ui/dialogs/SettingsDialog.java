package com.github.thkwag.thymelab.launcher.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import com.github.thkwag.thymelab.launcher.config.ConfigManager;
import java.util.Locale;
import java.util.ResourceBundle;
import com.github.thkwag.thymelab.launcher.ui.MainFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.github.thkwag.thymelab.launcher.ui.components.LogPanel;

public class SettingsDialog extends JDialog {
    private JComboBox<String> fontCombo;
    private JSpinner fontSizeSpinner;
    private JComboBox<String> languageCombo;
    private JTextField logBufferField;
    private final ConfigManager config;
    private ResourceBundle bundle;
    private static final String[] LABEL_KEYS = {"language", "font", "log_buffer_size", "font_size", "port", "lines"};
    private JLabel[] labels;
    private JButton saveButton;
    private JTextField portField;
    private JLabel logBufferUnitLabel;

    public SettingsDialog(Frame parent, ConfigManager config, ResourceBundle bundle) {
        super(parent, "Settings", true);
        this.config = config;
        this.bundle = bundle;
        
        setTitle(bundle.getString("menu_program_settings"));
        setResizable(false);
        
        initializeComponents();
        layoutComponents();
        initializeValues();
    }

    private void initializeComponents() {
        fontCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(12, 8, 72, 1));
        languageCombo = new JComboBox<>(new String[]{"EN", "KO"});
        logBufferField = new JTextField(5);
        portField = new JTextField(5);

        // Add listener for immediate application
        fontCombo.addActionListener(e -> {
            String selectedFont = (String) fontCombo.getSelectedItem();
            int selectedSize = (Integer) fontSizeSpinner.getValue();
            config.setProperty("font.family", selectedFont);
            config.setInt("font.size", selectedSize);
            updateParentUI();
        });

        fontSizeSpinner.addChangeListener(e -> {
            String selectedFont = (String) fontCombo.getSelectedItem();
            int selectedSize = (Integer) fontSizeSpinner.getValue();
            config.setProperty("font.family", selectedFont);
            config.setInt("font.size", selectedSize);
            updateParentUI();
        });

        languageCombo.addActionListener(e -> {
            String lang = (String) languageCombo.getSelectedItem();
            if (lang != null) {
                String code = lang.equals("KO") ? "ko" : "en";
                config.setProperty("language", code);
                config.save();
                // 설정창 자신의 언어도 업데이트
                this.bundle = ResourceBundle.getBundle("messages", 
                    Locale.forLanguageTag(code));
                updateTexts();
                // 메인창 업데이트
                updateParentUI();
            }
        });

        logBufferField.addActionListener(e -> {
            try {
                int bufferSize = Integer.parseInt(logBufferField.getText().trim());
                if (bufferSize > 0) {
                    config.setInt("log.buffer.size", bufferSize);
                    updateParentUI();
                }
            } catch (NumberFormatException ex) {
                // Ignore invalid input
            }
        });

        // Modify port field listener
        portField.getDocument().addDocumentListener(new DocumentListener() {
            private void updatePort() {
                try {
                    String text = portField.getText().trim();
                    if (!text.isEmpty()) {
                        int port = Integer.parseInt(text);
                        if (port >= 1024 && port <= 65535) {
                            config.setInt("server.port", port);
                            updateParentUI();
                        }
                    }
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) { updatePort(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updatePort(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updatePort(); }
        });
    }

    private void updateParentUI() {
        if (getParent() instanceof MainFrame) {
            MainFrame parent = (MainFrame) getParent();
            parent.updateFromSettings();
        }
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        
        // Initialize labels (fixed width)
        this.labels = new JLabel[LABEL_KEYS.length];
        for (int i = 0; i < LABEL_KEYS.length; i++) {
            labels[i] = new JLabel(bundle.getString(LABEL_KEYS[i]) + ":");
            labels[i].setPreferredSize(new Dimension(100, labels[i].getPreferredSize().height));
        }
        
        // Language
        gbc.gridy = 0;
        panel.add(labels[0], gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(languageCombo, gbc);
        
        // Buffer size
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(labels[2], gbc);
        
        JPanel bufferPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        bufferPanel.setPreferredSize(new Dimension(150, logBufferField.getPreferredSize().height));
        logBufferField.setPreferredSize(new Dimension(100, logBufferField.getPreferredSize().height));
        bufferPanel.add(logBufferField);
        logBufferUnitLabel = new JLabel(bundle.getString("lines"));
        bufferPanel.add(logBufferUnitLabel);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(bufferPanel, gbc);
        
        // Font settings
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(labels[1], gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(fontCombo, gbc);
        
        // Font size
        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(labels[3], gbc);
        
        JPanel fontSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        fontSizePanel.setPreferredSize(new Dimension(150, fontSizeSpinner.getPreferredSize().height));
        fontSizeSpinner.setPreferredSize(new Dimension(100, fontSizeSpinner.getPreferredSize().height));
        fontSizePanel.add(fontSizeSpinner);
        fontSizePanel.add(new JLabel("pt"));
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(fontSizePanel, gbc);
        
        // Port
        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(labels[4], gbc);
        
        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        portPanel.setPreferredSize(new Dimension(150, portField.getPreferredSize().height));
        portField.setPreferredSize(new Dimension(100, portField.getPreferredSize().height));
        portPanel.add(portField);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(portPanel, gbc);
        
        // Save button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        this.saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            saveSettings();
            setVisible(false);
        });
        buttonPanel.add(saveButton);
        
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getParent());
    }

    private void initializeValues() {
        // Load current settings
        String currentFont = config.getProperty("font.family", LogPanel.getDefaultMonospacedFont());
        int currentSize = config.getInt("font.size", 12);
        String currentLang = config.getProperty("language", "en");
        int currentBuffer = config.getInt("log.buffer.size", 1000);
        int currentPort = config.getInt("server.port", 8080);

        // Set current values to controls
        fontCombo.setSelectedItem(currentFont);
        fontSizeSpinner.setValue(currentSize);
        languageCombo.setSelectedItem(currentLang.equalsIgnoreCase("ko") ? "KO" : "EN");
        logBufferField.setText(String.valueOf(currentBuffer));
        portField.setText(String.valueOf(currentPort));
    }

    private void saveSettings() {
        // Save font settings
        String selectedFont = (String) fontCombo.getSelectedItem();
        int selectedSize = (Integer) fontSizeSpinner.getValue();
        config.setProperty("font.family", selectedFont);
        config.setInt("font.size", selectedSize);
        
        // Save language settings
        String lang = (String) languageCombo.getSelectedItem();
        config.setProperty("language", lang.equals("KO") ? "ko" : "en");
        
        // Save log buffer size
        try {
            int bufferSize = Integer.parseInt(logBufferField.getText().trim());
            if (bufferSize > 0) {
                config.setInt("log.buffer.size", bufferSize);
            }
        } catch (NumberFormatException ex) {
            // Ignore invalid input
        }
        
        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port >= 1024 && port <= 65535) {
                config.setInt("server.port", port);
            }
        } catch (NumberFormatException ex) {
            // Ignore invalid input
        }
    }

    private void updateTexts() {
        setTitle(bundle.getString("settings"));
        
        // 라벨 업데이트
        for (int i = 0; i < labels.length; i++) {
            labels[i].setText(bundle.getString(LABEL_KEYS[i]) + ":");
        }
        
        // Update unit label
        logBufferUnitLabel.setText(bundle.getString("lines"));
        
        saveButton.setText(bundle.getString("save"));
        pack();
    }
} 