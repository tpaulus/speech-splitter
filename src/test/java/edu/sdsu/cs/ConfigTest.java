package edu.sdsu.cs;

import com.amazonaws.auth.AWSCredentialsProvider;
import lombok.extern.log4j.Log4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Tom Paulus
 * Created on 4/25/18.
 */
@Log4j
public class ConfigTest {

    @Test
    public void getCredentials() {
        AWSCredentialsProvider provider = Config.getCredentials();
        assertNotNull(provider.getCredentials().getAWSAccessKeyId());
        assertNotNull(provider.getCredentials().getAWSSecretKey());

        log.debug("Access = " + provider.getCredentials().getAWSAccessKeyId());
        log.debug("Secret = " + provider.getCredentials().getAWSSecretKey());
    }
}