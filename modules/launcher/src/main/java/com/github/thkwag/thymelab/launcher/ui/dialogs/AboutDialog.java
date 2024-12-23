package com.github.thkwag.thymelab.launcher.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.github.thkwag.thymelab.launcher.config.ConfigManager;

public class AboutDialog extends JDialog {
    
    public AboutDialog(Frame parent, ResourceBundle bundle, ConfigManager config) {
        super(parent, bundle.getString("about_title"), true);
        setResizable(false);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        try {
            InputStream iconStream = getClass().getResourceAsStream("/icon.png");
            if (iconStream != null) {
                ImageIcon icon = new ImageIcon(ImageIO.read(iconStream));
                Image scaledImage = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
                iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(iconLabel);
                panel.add(Box.createVerticalStrut(15));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JLabel titleLabel = new JLabel(bundle.getString("app_title"));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subTitleLabel = new JLabel(bundle.getString("sub_title"));
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel versionLabel = new JLabel("Version " + config.getVersion());
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel copyrightLabel = new JLabel(bundle.getString("about_copyright"));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel licenseLabel = new JLabel("MIT License");
        licenseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        licenseLabel.setForeground(new Color(100, 100, 100));
        
        JLabel linkLabel = new JLabel("<html><a href='" + bundle.getString("about_link") + "'>" + 
            bundle.getString("about_link") + "</a></html>");
        linkLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(bundle.getString("about_link")));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subTitleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(versionLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(copyrightLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(licenseLabel);
        panel.add(Box.createVerticalStrut(10));
        
        // Add library information
        try (InputStream inputStream = getClass().getResourceAsStream("/libraries.txt")) {
            if (inputStream != null) {
                String librariesContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                JTextArea librariesText = new JTextArea(librariesContent);
                librariesText.setEditable(false);
                librariesText.setBackground(new Color(250, 250, 250));
                librariesText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                librariesText.setAlignmentX(Component.CENTER_ALIGNMENT);
                librariesText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                
                // Add text area to scroll pane
                JScrollPane scrollPane = new JScrollPane(librariesText);
                scrollPane.setPreferredSize(new Dimension(350, 120));
                scrollPane.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
                scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                panel.add(scrollPane);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Use panel to center GitHub link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        linkPanel.add(linkLabel);
        linkPanel.setOpaque(false);
        
        panel.add(Box.createVerticalStrut(10));
        panel.add(linkPanel);
        panel.add(Box.createVerticalStrut(20));
        
        JButton closeButton = new JButton(bundle.getString("about_close"));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> setVisible(false));
        panel.add(closeButton);
        
        add(panel);
        pack();
        setLocationRelativeTo(parent);
    }
} 