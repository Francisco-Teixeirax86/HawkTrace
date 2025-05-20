package hawkeye.hawktrace.hawkcollector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEvent {
    private String id;
    private String source;
    private String rawContent;
    private Instant timestamp;
    private String hostName;
    private String logType;
}
