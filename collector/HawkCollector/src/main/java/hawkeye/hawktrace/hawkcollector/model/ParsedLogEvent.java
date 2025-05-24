package hawkeye.hawktrace.hawkcollector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "hawtrace-logs")
public class ParsedLogEvent {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String rawContent;

    @Field(type = FieldType.Date)
    private Instant timestamp;

    @Field(type = FieldType.Date)
    private Instant collectionTime;

    @Field(type = FieldType.Keyword)
    private String source;

    @Field(type = FieldType.Keyword)
    private String logType;

    @Field(type = FieldType.Keyword)
    private String parserType;

    @Field(type = FieldType.Keyword)
    private String hostName;

    // Structured fields
    @Field(type = FieldType.Ip)
    private String sourceIp;

    @Field(type = FieldType.Ip)
    private String destinationIp;

    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Keyword)
    private String action;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Integer)
    private Integer statusCode;

    @Field(type = FieldType.Text)
    private String message;

    @Field(type = FieldType.Keyword)
    private String severity;

    @Field(type = FieldType.Text)
    private String userAgent;

    @Field(type = FieldType.Text)
    private String url;

    @Field(type = FieldType.Long)
    private Long responseSize;

    // Additional fields as a flexible map
    @Field(type = FieldType.Object)
    private Map<String, Object> additionalFields;

}
