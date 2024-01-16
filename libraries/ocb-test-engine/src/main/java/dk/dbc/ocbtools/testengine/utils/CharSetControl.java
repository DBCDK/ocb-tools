package dk.dbc.ocbtools.testengine.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class CharSetControl extends ResourceBundle.Control {
    private static final XLogger logger = XLoggerFactory.getXLogger(CharSetControl.class);
    private static String DEFAULT_CHARSET = "UTF-8";
    private String charset;

    public CharSetControl() {
        this(DEFAULT_CHARSET);
    }

    public CharSetControl(String charset) {
        this.charset = charset;
    }

    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        logger.entry(baseName, locale, format, loader);
        ResourceBundle bundle = null;

        ResourceBundle var20;
        try {
            String bundleName = this.toBundleName(baseName, locale);
            String resourceName = this.toResourceName(bundleName, "properties");
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }

            if (stream != null) {
                try {
                    logger.trace("Reading properties with charset {}", this.charset);
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, this.charset));
                } finally {
                    stream.close();
                }
            }

            var20 = bundle;
        } finally {
            logger.exit();
        }

        return var20;
    }
}
