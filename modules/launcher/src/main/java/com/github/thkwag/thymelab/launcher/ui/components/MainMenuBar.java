package com.github.thkwag.thymelab.launcher.ui.components;

import javax.swing.*;
import com.github.thkwag.thymelab.launcher.ui.MainForm;
import java.util.ResourceBundle;

public class MainMenuBar extends JMenuBar {
    private JMenu toolsMenu;
    private JMenu helpMenu;
    private JMenuItem programSettingsMenuItem;
    private JMenuItem thymeleafSettingsMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenuItem exitMenuItem;

    public MainMenuBar(MainForm mainForm, ResourceBundle bundle) {
        toolsMenu = new JMenu(bundle.getString("menu_tools"));
        helpMenu = new JMenu(bundle.getString("menu_help"));
        
        programSettingsMenuItem = new JMenuItem(bundle.getString("menu_program_settings"));
        thymeleafSettingsMenuItem = new JMenuItem(bundle.getString("menu_thymeleaf_settings"));
        aboutMenuItem = new JMenuItem(bundle.getString("menu_about"));
        
        toolsMenu.add(programSettingsMenuItem);
        toolsMenu.add(thymeleafSettingsMenuItem);
        toolsMenu.addSeparator();
        
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));
        toolsMenu.add(exitMenuItem);
        
        helpMenu.add(aboutMenuItem);
        
        add(toolsMenu);
        add(helpMenu);
    }

    public JMenuItem getProgramSettingsMenuItem() {
        return programSettingsMenuItem;
    }

    public JMenuItem getThymeleafSettingsMenuItem() {
        return thymeleafSettingsMenuItem;
    }

    public JMenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public JMenuItem getExitMenuItem() {
        return exitMenuItem;
    }

    public JMenu getToolsMenu() { return toolsMenu; }
    public JMenu getHelpMenu() { return helpMenu; }
} 