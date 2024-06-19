import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.json.JSONObject;

import java.util.Set;

public class JsonLayout extends Layout {

    private Set<String> fieldsToMask;

    public void setFieldsToMask(Set<String> fieldsToMask) {
        this.fieldsToMask = fieldsToMask;
    }

    @Override
    public String format(LoggingEvent event) {
        JSONObject logEntry = new JSONObject();
        logEntry.put("timestamp", event.timeStamp);
        logEntry.put("level", event.getLevel().toString());
        logEntry.put("logger", event.getLoggerName());
        logEntry.put("thread", event.getThreadName());

        // Java Beans 객체를 JSON으로 변환하여 로그에 추가
        Object message = event.getMessage();
        if (message instanceof MyBean) {
            try {
                String json = JsonMaskingUtil.maskFields(message, fieldsToMask);
                logEntry.put("message", new JSONObject(json));
            } catch (Exception e) {
                logEntry.put("message", "Error converting bean to JSON: " + e.getMessage());
            }
        } else {
            logEntry.put("message", event.getRenderedMessage());
        }

        if (event.getThrowableInformation() != null) {
            logEntry.put("exception", String.join("\n", event.getThrowableInformation().getThrowableStrRep()));
        }

        return logEntry.toString() + System.lineSeparator();
    }

    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    @Override
    public void activateOptions() {
        // No options to activate
    }
}
