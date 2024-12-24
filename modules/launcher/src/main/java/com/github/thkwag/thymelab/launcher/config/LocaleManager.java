package com.github.thkwag.thymelab.launcher.config;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class LocaleManager {
    private static final Logger logger = Logger.getLogger(LocaleManager.class.getName());
    private Locale currentLocale;
    private ResourceBundle bundle;

    public LocaleManager(String lang) {
        setLanguage(lang);
    }

    public ResourceBundle getBundle() {
        if (bundle == null) {
            loadBundle();
        }
        return bundle;
    }

    private void loadBundle() {
        try {
            bundle = ResourceBundle.getBundle("messages", currentLocale, this.getClass().getClassLoader());
        } catch (MissingResourceException e) {
            logger.warning("Failed to load resource bundle for locale " + currentLocale + ": " + e.getMessage());
            try {
                bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), this.getClass().getClassLoader());
            } catch (MissingResourceException ex) {
                logger.severe("Failed to load fallback resource bundle: " + ex.getMessage());
                throw new RuntimeException("Failed to load any resource bundle", ex);
            }
        }
    }

    public void setLanguage(String lang) {
        Locale newLocale = Locale.forLanguageTag(lang);
        if (!newLocale.equals(currentLocale)) {
            currentLocale = newLocale;
            bundle = null;  // Invalidate bundle when language changes
        }
    }

    public String getCurrentLanguage() {
        return currentLocale.toLanguageTag();
    }
} 