/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac;

import com.prodyna.pac.timetracker.server.rest.AbstractRESTTest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for {@code Arquillian} test tasks.
 *
 * @author apatrikis
 * @see
 * <a href="https://blog.david-belzer.de/2013/10/arquillian-mit-maven-java-ee7-und-glassfish-4/">Glassfish
 * 4 and Arquillian</a>
 * @see
 * <a href="http://arquillian.org/blog/2012/04/13/the-danger-of-embedded-containers/">Pitfalls
 * of using embedded Arquillian</a>
 * @see <a href="http://arquillian.org/guides/testing_java_persistence/">JPA
 * Persitsnce in Arquillian tests</a>
 * @see <a href="https://issues.jboss.org/browse/ARQ-567">Arquillian Test Suite
 * support under development</a>
 * @see
 * <a href="https://github.com/ingwarsw/arquillian-suite-extension">Arquillian
 * Test Suite support from 3rd party</a>
 */
public class ArquillianHelper {

    public static final String ARCHIVE_FILE_NAME = "timetracker-server-arquillian";

    private static final Logger log = LoggerFactory.getLogger(ArquillianHelper.class);

    /**
     * Create a {@link WebArchive} for executing the {@code Arquillian} tests.
     * The name of the crated file is defines in {@link #ARCHIVE_FILE_NAME}.
     *
     * @return The creates {@link WebArchive} for test execution.
     */
    public static final WebArchive createDeployment() {
        // get all maven dependecies
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, ARCHIVE_FILE_NAME + ".war")
                .addAsLibraries(files)
                .addPackages(true, "com.prodyna.pac")
                .addAsResource(new File("src/test/resources-glassfish-managed/logback-test.xml"), "logback-test.xml")
                .addAsResource(new File("src/test/resources-glassfish-managed/persistence-test.xml"), "META-INF/persistence.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"))
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/glassfish-web.xml"))
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"));

        setArquillianBaseURL();

        return archive;
    }

    /**
     * Save the archive structure to a file. This does <b>not</b> create the
     * archive with ist content.
     *
     * @param archive The archive for which to dump the structure.
     * @param target The file to save the archive structure.
     */
    public static void saveContentStructure(WebArchive archive, File target) {
        try (OutputStream out = new FileOutputStream(target)) {
            log.debug(String.format("Writing archive contents [%s] to file [%s]", archive.getName(), target.getAbsoluteFile()));
            archive.writeTo(out, Formatters.VERBOSE);
        }
        catch (Exception e) {
            log.error(String.format("Error writing archive contents [%s] to file [%s]", archive.getName(), target.getAbsoluteFile()), e);
        }
    }

    /**
     * IMPORTANT: ensure the base URL is set up for executing Arquillian tests.
     */
    private static void setArquillianBaseURL() {
        AbstractRESTTest.BASE_URL = AbstractRESTTest.BASE_URL_ARQUILLIAN;
    }
}
