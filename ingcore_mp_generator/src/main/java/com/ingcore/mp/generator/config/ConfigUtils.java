package com.ingcore.mp.generator.config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ibrmy-work on 2019/8/20.
 */
public class ConfigUtils {

    static String projectFilePath = "generatorConfig.properties";

    static Properties props = new Properties();

    static {
        String filePath = ConfigUtils.class.getResource("/" + projectFilePath).toString();

        filePath = filePath.substring(6);
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readProperties(String key) {

        String value = props.getProperty(key);

        return value;
    }
}
