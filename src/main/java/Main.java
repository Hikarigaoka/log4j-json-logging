import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.log4j.Logger;


import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        Person person = new Person("John Doe", "123-45-6789", "john.doe@example.com");
        Address address = new Address("123 Main St", "Springfield", "12345");

        Set<String> personFieldsToMask = new HashSet<>();
        personFieldsToMask.add("ssn");
        personFieldsToMask.add("email");

        Set<String> addressFieldsToMask = new HashSet<>();
        addressFieldsToMask.add("postalCode");

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Person.class, new CustomSerializer<>(personFieldsToMask));
        module.addSerializer(Address.class, new CustomSerializer<>(addressFieldsToMask));
        objectMapper.registerModule(module);

        try {
            // 오브젝트를 JSON으로 시리얼라이즈
            String personJson = objectMapper.writeValueAsString(person);
            String addressJson = objectMapper.writeValueAsString(address);
            
            logger.info("시리얼라이즈된 Person JSON: " + personJson);
            logger.info("시리얼라이즈된 Address JSON: " + addressJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    private static void addShutdownHook() {
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
}
