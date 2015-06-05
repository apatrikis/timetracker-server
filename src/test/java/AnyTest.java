/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.prodyna.pac.timetracker.Security;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author apatrikis
 */
public class AnyTest {

    @Test
    public void testHashingAlgorithm() throws NoSuchAlgorithmException {
        String own = Security.passwordHashSHA256("password1");
        // from "glassfish-embedded-all-4.1.jar" -> Google "guava" lib
        String lib = Hashing.sha256().hashString("password1", Charsets.UTF_8).toString();

        Assert.assertTrue(own.equals(lib));
    }

    @Test
    public void testDateFormatParsing() {
        Instant i = Instant.parse("2007-12-03T10:15:30.00Z");
        Date.from(i);
    }
}
