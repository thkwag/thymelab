package com.github.thkwag.thymelab.launcher.ui.dialogs;

import com.github.thkwag.thymelab.launcher.config.ConfigManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ResourceBundle;

public class ThymeleafSettingsDialog extends JDialog {
    private final ConfigManager config;
    private final ResourceBundle bundle;
    private JTextField staticFolderField;
    private JTextField templatesFolderField;
    private JTextField dataFolderField;
    private JButton saveButton;

    // Configuration keys
    private static final String KEY_STATIC = "static.folder.path";
    private static final String KEY_TEMPLATES = "templates.folder.path";
    private static final String KEY_DATA = "data.folder.path";

    // Layout constants
    private static final int BORDER_PADDING_TOP = 10;
    private static final int BORDER_PADDING_SIDES = 15;
    private static final int BORDER_PADDING_BOTTOM = 5;
    private static final int COMPONENT_SPACING = 5;
    private static final int TEXT_FIELD_COLUMNS = 40;

    // Grid constants
    private static final int GRID_SPACING = 2;

    public ThymeleafSettingsDialog(Frame parent, ResourceBundle bundle, ConfigManager config) {
        super(parent, bundle.getString("menu_thymeleaf_settings"), true);
        this.config = config;
        this.bundle = bundle;
        
        // Reload configuration file
        config.loadProperties();
        
        setResizable(false);
        initializeComponents();
        layoutComponents();
        initializeValues();
    }

    private void initializeComponents() {
        staticFolderField = new JTextField(TEXT_FIELD_COLUMNS);
        templatesFolderField = new JTextField(TEXT_FIELD_COLUMNS);
        dataFolderField = new JTextField(TEXT_FIELD_COLUMNS);
        saveButton = new JButton(bundle.getString("save"));
    }

    private void layoutComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(
            BORDER_PADDING_TOP, BORDER_PADDING_SIDES, 
            BORDER_PADDING_BOTTOM, BORDER_PADDING_SIDES));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(COMPONENT_SPACING, COMPONENT_SPACING, 
            COMPONENT_SPACING, COMPONENT_SPACING);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Static folder
        addFolderRow(panel, gbc, 0, 
            bundle.getString("static_folder"),
            bundle.getString("folder_description_static"),
            staticFolderField);
            
        // Templates folder
        addFolderRow(panel, gbc, GRID_SPACING,
            bundle.getString("templates_folder"),
            bundle.getString("folder_description_templates"),
            templatesFolderField);
            
        // Data folder
        addFolderRow(panel, gbc, GRID_SPACING * 2,
            bundle.getString("data_folder"),
            bundle.getString("folder_description_data"),
            dataFolderField);

        // Save button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(
            COMPONENT_SPACING, 0, COMPONENT_SPACING, 0));
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

    private void addFolderRow(JPanel panel, GridBagConstraints gbc, int gridy, 
                            String label, String description, JTextField field) {
        gbc.gridy = gridy;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(new JLabel(label), gbc);

        gbc.gridy = gridy + 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);

        JButton browseButton = new JButton(bundle.getString("select_folder"));
        browseButton.addActionListener(e -> selectFolder(field));
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        panel.add(browseButton, gbc);
    }

    private void selectFolder(JTextField field) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(bundle.getString("select_folder"));
        
        String currentPath = field.getText();
        if (!currentPath.isEmpty()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists()) {
                chooser.setCurrentDirectory(currentDir);
            }
        }
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void initializeValues() {
        // Load saved settings
        String staticPath = config.getProperty(KEY_STATIC, "");
        String templatesPath = config.getProperty(KEY_TEMPLATES, "");
        String dataPath = config.getProperty(KEY_DATA, "");

        // System.out.println("Loading settings - static: " + staticPath);
        // System.out.println("Loading settings - templates: " + templatesPath);
        // System.out.println("Loading settings - data: " + dataPath);

        // Set text fields
        staticFolderField.setText(staticPath);
        templatesFolderField.setText(templatesPath);
        dataFolderField.setText(dataPath);

        // Set tooltips
        staticFolderField.setToolTipText(staticPath);
        templatesFolderField.setToolTipText(templatesPath);
        dataFolderField.setToolTipText(dataPath);
    }

    private void saveSettings() {
        String staticPath = staticFolderField.getText().trim();
        String templatesPath = templatesFolderField.getText().trim();
        String dataPath = dataFolderField.getText().trim();

        System.out.println("Saving settings - static: " + staticPath);
        System.out.println("Saving settings - templates: " + templatesPath);
        System.out.println("Saving settings - data: " + dataPath);

        config.setProperty(KEY_STATIC, staticPath);
        config.setProperty(KEY_TEMPLATES, templatesPath);
        config.setProperty(KEY_DATA, dataPath);
        config.save();
    }
} 