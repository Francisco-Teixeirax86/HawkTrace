package hawkeye.hawktrace.hawkcollector.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hawkeye.hawktrace.hawkcollector.Utils.Constants;
import hawkeye.hawktrace.hawkcollector.model.ParsedLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JsonParser implements LogParser{

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public boolean canParse(String log) {
        if (log == null || log.trim().isEmpty())
            return false;

        String trimmed = log.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    @Override
    public ParsedLogEvent parse(String rawline) {
        try {
            JsonNode jsonNode = objectMapper.readTree(rawline);

            return ParsedLogEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .rawContent(rawline)
                    .parserType(Constants.jsonParserType)
                    .timestamp(extractTimestamp(jsonNode))
                    .collectionTime(Instant.now())
                    .collectionTime(Instant.now())
                    .sourceIp(extractField(jsonNode, "source_ip", "client_ip", "ip"))
                    .destinationIp(extractField(jsonNode, "dest_ip", "destIp", "serverIp"))
                    .username(extractField(jsonNode, "username", "user", "user_name"))
                    .action(extractField(jsonNode, "action", "method", "event_type"))
                    .status(extractField(jsonNode, "status", "result", "outcome"))
                    .statusCode(extractIntField(jsonNode, "status_code", "code", "response_code"))
                    .message(extractField(jsonNode, "message", "msg", "description", "text"))
                    .severity(extractSeverity(jsonNode))
                    .userAgent(extractField(jsonNode, "user_agent", "userAgent"))
                    .url(extractField(jsonNode, "url", "path", "request_uri"))
                    .responseSize(extractLongField(jsonNode, "response_size", "size", "bytes"))
                    .additionalFields(extractAdditionalFields(jsonNode))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse JSON log: {} - Error: {}", rawline, e.getMessage());
            return createFallbackEvent(rawline);
        }
    }

    private String extractField(JsonNode jsonNode, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (jsonNode.has(fieldName)) {
                JsonNode fieldNode = jsonNode.get(fieldName);
                if (!fieldNode.isNull()) {
                    return fieldNode.asText();
                }
            }
        }
        return null;
    }

    private Instant extractTimestamp(JsonNode jsonNode){
        String[] timestampFields = {"timestamp", "@timestamp", "time", "datetime", "created_at"};

        for (String fieldName : timestampFields) {
            if (jsonNode.has(fieldName)) {
                String fieldValue = jsonNode.get(fieldName).asText();

                //Try different formats
                try {
                    return Instant.parse(fieldValue);
                } catch (DateTimeParseException e) {
                    try {
                        return Instant.ofEpochSecond(Long.parseLong(fieldValue));
                    } catch (NumberFormatException e2) {
                        try {
                            return Instant.ofEpochMilli(Long.parseLong(fieldValue));
                        } catch (NumberFormatException e3) {
                            log.warn("Could not parse timestamp: {}", fieldValue);
                        }
                    }
                }
            }
        }

        return Instant.now(); //Fallback
    }

    public Integer extractIntField(JsonNode jsonNode, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (jsonNode.has(fieldName)) {
                JsonNode fieldNode = jsonNode.get(fieldName);
                if (fieldNode.isInt()) {
                    return fieldNode.asInt();
                }
                try {
                    return Integer.parseInt(fieldNode.asText());
                } catch (NumberFormatException e) {
                    log.warn("Could not parse int: {}", fieldNode.asText());
                }
            }
        }
        return null;
    }

    private Long extractLongField(JsonNode jsonNode, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (jsonNode.has(fieldName)) {
                JsonNode fieldNode = jsonNode.get(fieldName);
                if (fieldNode.isLong() || fieldNode.isInt()) {
                    return fieldNode.asLong();
                }
                try {
                    return Long.parseLong(fieldNode.asText());
                } catch (NumberFormatException e) {
                    log.warn("Could not parse int: {}", fieldNode.asText());
                }
            }
        }
        return null;
    }

    private String extractSeverity(JsonNode jsonNode) {
        String[] severityFields = {"severity", "level", "priority", "log_level"};

        for (String field : severityFields) {
            if (jsonNode.has(field)) {
                String severity = jsonNode.get(field).asText().toLowerCase();

                // Normalized severity levels
                if (severity.contains("error") || severity.contains("err")) {
                    return Constants.errorSeverity;
                } else if (severity.contains("warn")) {
                    return Constants.warnSeverity;
                } else if (severity.contains("info")) {
                    return Constants.infoSeverity;
                } else if (severity.contains("debug")) {
                    return Constants.debugSeverity;
                } else if (severity.contains("critical") || severity.contains("fatal")) {
                    return Constants.criticalSeverity;
                }

                return severity.toUpperCase();
            }
        }

        return Constants.infoSeverity; // Default severity
    }

    private Map<String, Object> extractAdditionalFields(JsonNode jsonNode) {
        Map<String, Object> additionalFields = new HashMap<>();

        //TODO change this so every field is put on a HASHMAP that we simply acess across the class for O(1) lookups instead
        //of another array, but right now I can't be bothered :(
        String[] standardFields = {
                "timestamp", "@timestamp", "time", "datetime", "created_at",
                "source_ip", "sourceIp", "client_ip", "ip",
                "dest_ip", "destIp", "server_ip",
                "username", "user", "user_name",
                "action", "method", "event_type",
                "status", "result", "outcome",
                "status_code", "code", "response_code",
                "message", "msg", "description", "text",
                "severity", "level", "priority", "log_level",
                "user_agent", "userAgent",
                "url", "path", "request_uri",
                "response_size", "size", "bytes"
        };

        // Add all fields that aren't in the standard set
        jsonNode.fieldNames().forEachRemaining(fieldName -> {
            boolean isStandardField = false;
            for (String standardField : standardFields) {
                if (standardField.equals(fieldName)) {
                    isStandardField = true;
                    break;
                }
            }

            if (!isStandardField) {
                JsonNode fieldValue = jsonNode.get(fieldName);
                if (fieldValue.isTextual()) {
                    additionalFields.put(fieldName, fieldValue.asText());
                } else if (fieldValue.isNumber()) {
                    additionalFields.put(fieldName, fieldValue.asLong());
                } else if (fieldValue.isBoolean()) {
                    additionalFields.put(fieldName, fieldValue.asBoolean());
                } else {
                    additionalFields.put(fieldName, fieldValue.toString());
                }
            }
        });

        return additionalFields.isEmpty() ? null : additionalFields;
    }

    private ParsedLogEvent createFallbackEvent(String rawLog) {
        return ParsedLogEvent.builder()
                .id(UUID.randomUUID().toString())
                .rawContent(rawLog)
                .parserType("json-fallback")
                .timestamp(Instant.now())
                .collectionTime(Instant.now())
                .message(rawLog)
                .severity(Constants.infoSeverity)
                .build();
    }

    @Override
    public String getParserType() {
        return Constants.jsonParserType;
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
