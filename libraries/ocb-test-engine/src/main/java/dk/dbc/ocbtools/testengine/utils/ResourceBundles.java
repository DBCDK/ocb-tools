package dk.dbc.ocbtools.testengine.utils;

import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class ResourceBundles {
    private static final XLogger logger = XLoggerFactory.getXLogger(ResourceBundles.class);
    public static Locale DANISH = new Locale("da", "DK");

    public ResourceBundles() {
    }

    public static ResourceBundle getBundle(String bundleName) {
        return getBundle(bundleName, DANISH);
    }

    public static ResourceBundle getBundle(String bundleName, Locale locale) {
        logger.entry(bundleName, locale);

        ResourceBundle var2;
        try {
            var2 = ResourceBundle.getBundle(bundleName, locale, new CharSetControl());
        } finally {
            logger.exit();
        }

        return var2;
    }

    public static ResourceBundle getBundle(ClassLoader classloader, String bundleName) {
        return getBundle(classloader, bundleName, DANISH);
    }

    public static ResourceBundle getBundle(ClassLoader classloader, String bundleName, Locale locale) {
        logger.entry(classloader, bundleName, locale);

        ResourceBundle var3;
        try {
            var3 = ResourceBundle.getBundle(bundleName, locale, classloader, new CharSetControl());
        } finally {
            logger.exit();
        }

        return var3;
    }

    public static ResourceBundle getBundle(Object owner, String bundleName) {
        return getBundle(owner.getClass().getClassLoader(), owner.getClass().getPackage().getName() + "." + bundleName);
    }
}
