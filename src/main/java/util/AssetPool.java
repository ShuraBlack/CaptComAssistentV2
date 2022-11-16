package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ShuraBlack
 * @since 03-20-2022
 */
public class AssetPool {

    private static final Logger LOGGER = LogManager.getLogger(AssetPool.class);

    private static final Map<String, File> IMAGES = new ConcurrentHashMap<>();
    private static final Map<String, String> URLS = new ConcurrentHashMap<>();

    public static void init() {
        LOGGER.info("Try to load media for AssetPool <\u001b[32;1massetpool.properties\u001b[0m>");
        Properties properties = FileUtil.loadProperties("assetpool.properties");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith("img")) {
                IMAGES.put(key, new File(value));
            } else if (key.startsWith("url")) {
                URLS.put(key,value);
            }
        }
        LOGGER.info("Successfully finished to load media for AssetPool");
    }

    public static void clear() {
        IMAGES.clear();
        URLS.clear();
    }

    public static File getImage(@Nonnull final String key) {
        return IMAGES.get(key);
    }

    public static String getURL(@Nonnull final String key) {
        return URLS.get(key);
    }
}
