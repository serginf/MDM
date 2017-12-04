package eu.supersede.mdm.storage.util;

import eu.supersede.mdm.storage.ApacheMain;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by snadal on 16/06/17.
 */
public class ConfigManager {

    public static String getProperty(String property) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream(ApacheMain.configPath));
            return prop.getProperty(property);
        } catch (IOException ex) {
            return null;
        }
    }

}
