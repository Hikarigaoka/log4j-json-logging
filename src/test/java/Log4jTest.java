import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;

import static org.junit.Assert.assertTrue;

public class Log4jTest {

    private static final Logger logger = Logger.getLogger(Log4jTest.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File log4jConfigFile;

    @Before
    public void setup() throws IOException {
        log4jConfigFile = tempFolder.newFile("log4j.xml");
        Files.write(log4jConfigFile.toPath(), getLog4jConfig().getBytes());

        System.setProperty("log4j.configuration", log4jConfigFile.toURI().toString());
        DOMConfigurator.configure(log4jConfigFile.toString());
    }

    @After
    public void cleanup() {
        LogManager.shutdown();
    }

    @Test
    public void testLogBuffer() throws IOException {
        for (int i = 0; i < 1000; i++) {
            logger.info("Test log message " + i);
        }

        File logFile = new File(tempFolder.getRoot(), "logs/app.log");
        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);
    }

    @Test
    public void testShutdownHook() {
        addShutdownHook();

        // JVM 종료를 트리거하여 종료 훅을 테스트
        Runtime.getRuntime().halt(0); // System.exit(0)을 사용할 경우 JUnit이 종료됨
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger rootLogger = Logger.getRootLogger();
            if (rootLogger != null) {
                Enumeration<?> appenders = rootLogger.getAllAppenders();
                while (appenders.hasMoreElements()) {
                    Appender appender = (Appender) appenders.nextElement();
                    if (appender instanceof AsyncAppender) {
                        ((AsyncAppender) appender).close();
                    } else {
                        appender.close();
                    }
                }
            }
        }));
    }

    private String getLog4jConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">\n" +
                "<log4j:configuration xmlns:log4j=\"http://jakarta.apache.org/log4j/\">\n" +
                "    <appender name=\"AUTO_RESIZE_ASYNC\" class=\"AutoResizeAsyncAppender\">\n" +
                "        <param name=\"InitialBufferSize\" value=\"10\" />\n" +
                "        <param name=\"MaxBufferSize\" value=\"100\" />\n" +
                "        <param name=\"MinBufferSize\" value=\"10\" />\n" +
                "        <param name=\"ResizeFactor\" value=\"10\" />\n" +
                "        <appender-ref ref=\"FILE\" />\n" +
                "    </appender>\n" +
                "    <appender name=\"FILE\" class=\"org.apache.log4j.FileAppender\">\n" +
                "        <param name=\"File\" value=\"logs/app.log\" />\n" +
                "        <param name=\"Append\" value=\"true\" />\n" +
                "        <layout class=\"org.apache.log4j.PatternLayout\">\n" +
                "            <param name=\"ConversionPattern\" value=\"%d{ISO8601} %-5p [%t] %c: %m%n\" />\n" +
                "        </layout>\n" +
                "    </appender>\n" +
                "    <root>\n" +
                "        <priority value=\"info\" />\n" +
                "        <appender-ref ref=\"AUTO_RESIZE_ASYNC\" />\n" +
                "    </root>\n" +
                "</log4j:configuration>";
    }
}
