import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class JsonMaskingUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MASK = "***";

    public static String maskFields(Object bean, Set<String> fieldsToMask) throws IOException {
        String json = objectMapper.writeValueAsString(bean);
        JsonNode root = objectMapper.readTree(json);
        maskFieldsRecursive(root, fieldsToMask);
        return objectMapper.writeValueAsString(root);
    }

    private static void maskFieldsRecursive(JsonNode node, Set<String> fieldsToMask) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> field = fields.next();
                if (fieldsToMask.contains(field.getKey())) {
                    ((ObjectNode) node).put(field.getKey(), MASK);
                } else {
                    maskFieldsRecursive(field.getValue(), fieldsToMask);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                maskFieldsRecursive(arrayElement, fieldsToMask);
            }
        }
    }
}
