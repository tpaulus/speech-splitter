package edu.sdsu.cs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
public class Config {
    public static final String CONFIG_FILE_NAME = "aws.properties";

    public static AWSCredentialsProvider getCredentials() {
        try {
            Properties properties = new Properties();
            properties.load(Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME));

            return new AWSStaticCredentialsProvider(new AWSCredentials() {
                @Override public String getAWSAccessKeyId() {
                    return properties.getProperty("global.auth.access");
                }

                @Override public String getAWSSecretKey() {
                    return properties.getProperty("global.auth.secret");
                }
            });
        } catch (IOException e) {
            log.error("Could not open AWS Config");
            throw new RuntimeException("Could not open Pipeline Config", e);
        }
    }

    public static String getProperty(final String key) {
        try {
            Properties properties = new Properties();
            properties.load(Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME));
            return properties.getProperty(key);
        } catch (IOException e) {
            log.error("Could not open AWS Config");
            throw new RuntimeException("Could not open AWS Config", e);
        }
    }

    public static String getRegion() {
        try {
            Properties properties = new Properties();
            properties.load(Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME));
            return properties.getProperty("global.region");
        } catch (IOException e) {
            log.error("Could not open AWS Config");
            throw new RuntimeException("Could not open AWS Config", e);
        }
    }
}
