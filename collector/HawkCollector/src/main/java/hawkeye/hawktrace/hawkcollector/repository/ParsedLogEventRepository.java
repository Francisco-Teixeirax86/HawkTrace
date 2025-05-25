package hawkeye.hawktrace.hawkcollector.repository;

import hawkeye.hawktrace.hawkcollector.model.ParsedLogEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ParsedLogEventRepository extends ElasticsearchRepository<ParsedLogEvent, String> {

    List<ParsedLogEvent> findBySourceIp(String sourceIp);
    List<ParsedLogEvent> findBySeverity(String severity);
    List<ParsedLogEvent> findByParserType(String parserType);
    List<ParsedLogEvent> findByTimestampBetween(Instant from, Instant to);
    List<ParsedLogEvent> findBySourceIpAndSeverity(String sourceIp, String severity);
}
