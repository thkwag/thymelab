package com.github.thkwag.thymelab.launcher.ui.dialogs;

import com.github.thkwag.thymelab.launcher.config.ConfigManager;
import com.github.thkwag.thymelab.launcher.ui.MainFrame;
import com.github.thkwag.thymelab.launcher.ui.components.LogPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

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
    private JSpinner portSpinner;
    private JLabel logBufferUnitLabel;

    // Port settings
    private static final int MIN_PORT = 1024;
    private static final int MAX_PORT = 65535;
    private static final int DEFAULT_PORT = 8080;

    // Font settings
    private static final int MIN_FONT_SIZE = 8;
    private static final int MAX_FONT_SIZE = 72;
    private static final int DEFAULT_FONT_SIZE = 12;
    private static final int FONT_SIZE_STEP = 1;

    // Component dimensions
    private static final int LABEL_WIDTH = 100;
    private static final int FIELD_WIDTH = 100;
    private static final int PANEL_WIDTH = 150;
    private static final int TEXT_FIELD_COLUMNS = 5;

    // Default values
    private static final int DEFAULT_BUFFER_SIZE = 1000;
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String[] SUPPORTED_LANGUAGES = {"EN", "KO"};

    // Layout constants
    private static final int BORDER_PADDING = 10;
    private static final int BORDER_BOTTOM = 5;
    private static final int COMPONENT_SPACING = 5;
    private static final int FLOW_HGAP = 2;
    private static final int FLOW_VGAP = 0;

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
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_FONT_SIZE, MIN_FONT_SIZE, MAX_FONT_SIZE, FONT_SIZE_STEP));
        languageCombo = new JComboBox<>(SUPPORTED_LANGUAGES);
        logBufferField = new JTextField(TEXT_FIELD_COLUMNS);
        portSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_PORT, MIN_PORT, MAX_PORT, 1));
        JSpinner.NumberEditor portEditor = new JSpinner.NumberEditor(portSpinner, "#");
        portSpinner.setEditor(portEditor);
        ((JSpinner.DefaultEditor) portSpinner.getEditor()).getTextField().setColumns(TEXT_FIELD_COLUMNS);

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
                // Update dialog language
                this.bundle = ResourceBundle.getBundle("messages", 
                    Locale.forLanguageTag(code));
                updateTexts();
                // Update main window
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

        portSpinner.addChangeListener(e -> {
            int port = (Integer) portSpinner.getValue();
            config.setInt("server.port", port);
            updateParentUI();
        });
    }

    private void updateParentUI() {
        if (getParent() instanceof MainFrame parent) {
            parent.updateFromSettings();
        }
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_BOTTOM, BORDER_PADDING));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(COMPONENT_SPACING, COMPONENT_SPACING, COMPONENT_SPACING, COMPONENT_SPACING);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        
        // Initialize labels (fixed width)
        this.labels = new JLabel[LABEL_KEYS.length];
        for (int i = 0; i < LABEL_KEYS.length; i++) {
            labels[i] = new JLabel(bundle.getString(LABEL_KEYS[i]) + ":");
            labels[i].setPreferredSize(new Dimension(LABEL_WIDTH, labels[i].getPreferredSize().height));
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
        
        JPanel bufferPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_HGAP, FLOW_VGAP));
        bufferPanel.setPreferredSize(new Dimension(PANEL_WIDTH, logBufferField.getPreferredSize().height));
        logBufferField.setPreferredSize(new Dimension(FIELD_WIDTH, logBufferField.getPreferredSize().height));
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
        
        JPanel fontSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_HGAP, FLOW_VGAP));
        fontSizePanel.setPreferredSize(new Dimension(PANEL_WIDTH, fontSizeSpinner.getPreferredSize().height));
        fontSizeSpinner.setPreferredSize(new Dimension(FIELD_WIDTH, fontSizeSpinner.getPreferredSize().height));
        fontSizePanel.add(fontSizeSpinner);
        fontSizePanel.add(new JLabel("pt"));
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(fontSizePanel, gbc);
        
        // Port
        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(labels[4], gbc);
        
        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_HGAP, FLOW_VGAP));
        portPanel.setPreferredSize(new Dimension(PANEL_WIDTH, portSpinner.getPreferredSize().height));
        portSpinner.setPreferredSize(new Dimension(FIELD_WIDTH, portSpinner.getPreferredSize().height));
        portPanel.add(portSpinner);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(portPanel, gbc);
        
        // Save button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(COMPONENT_SPACING, 0, COMPONENT_SPACING, 0));
        this.saveButton = new JButton(bundle.getString("save"));
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
        int currentSize = config.getInt("font.size", DEFAULT_FONT_SIZE);
        String currentLang = config.getProperty("language", DEFAULT_LANGUAGE);
        int currentBuffer = config.getInt("log.buffer.size", DEFAULT_BUFFER_SIZE);
        int currentPort = config.getInt("server.port", DEFAULT_PORT);

        // Set current values to controls
        fontCombo.setSelectedItem(currentFont);
        fontSizeSpinner.setValue(currentSize);
        languageCombo.setSelectedItem(currentLang.equalsIgnoreCase("ko") ? "KO" : "EN");
        logBufferField.setText(String.valueOf(currentBuffer));
        portSpinner.setValue(currentPort);
    }

    private void saveSettings() {
        // Save font settings
        String selectedFont = (String) fontCombo.getSelectedItem();
        int selectedSize = (Integer) fontSizeSpinner.getValue();
        config.setProperty("font.family", selectedFont);
        config.setInt("font.size", selectedSize);
        
        // Save language settings
        String lang = (String) languageCombo.getSelectedItem();
        assert lang != null;
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
        
        // Save port setting
        int port = (Integer) portSpinner.getValue();
        if (port >= MIN_PORT && port <= MAX_PORT) {
            config.setInt("server.port", port);
        }
    }

    private void updateTexts() {
        setTitle(bundle.getString("settings"));
        
        // Update labels
        for (int i = 0; i < labels.length; i++) {
            labels[i].setText(bundle.getString(LABEL_KEYS[i]) + ":");
        }
        
        // Update unit label
        logBufferUnitLabel.setText(bundle.getString("lines"));
        
        saveButton.setText(bundle.getString("save"));
        pack();
    }
} 