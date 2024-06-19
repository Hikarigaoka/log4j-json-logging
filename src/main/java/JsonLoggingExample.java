import org.apache.log4j.Logger;
import java.util.HashSet;
import java.util.Set;

public class JsonLoggingExample {
    private static final Logger logger = Logger.getLogger(JsonLoggingExample.class);

    public static void main(String[] args) {
        MyBean bean = new MyBean("John Doe", 30, "john.doe@example.com", "secret");

        Set<String> fieldsToMask = new HashSet<>();
        fieldsToMask.add("password");
        fieldsToMask.add("email");

        JsonLayout jsonLayout = new JsonLayout();
        jsonLayout.setFieldsToMask(fieldsToMask);

        logger.info(bean);

        logger.error("This is an error message", new RuntimeException("Example exception"));
    }
}
