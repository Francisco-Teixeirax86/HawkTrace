package hawkeye.hawktrace.hawkcollector.service;

import hawkeye.hawktrace.hawkcollector.model.ParsedLogEvent;
import hawkeye.hawktrace.hawkcollector.repository.ParsedLogEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.attribute.standard.Severity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ElasticsearchService {

    private final ParsedLogEventRepository repository;

    @Autowired
    public ElasticsearchService(ParsedLogEventRepository repository) {
        this.repository = repository;
    }

    public ParsedLogEvent saveParsedLogEvent(ParsedLogEvent parsedLogEvent) {
        try {
            ParsedLogEvent saved = repository.save(parsedLogEvent);
            log.debug("Sved parsed event to elasticsearch: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Error while saving parsed log event", e);
            throw e;
        }
    }

    public Optional<ParsedLogEvent> findById(String id) {
        return repository.findById(id);
    }

    public List<ParsedLogEvent> findBySourceIp(String sourceIp) {
        return repository.findBySourceIp(sourceIp);
    }

    public List<ParsedLogEvent> findBySeverity(String severity) {
        return repository.findBySeverity(severity);
    }

    public List<ParsedLogEvent> findBySourceIpAndSeverity(String sourceIP, String severity) {return repository.findBySourceIpAndSeverity(sourceIP, severity);}

    public List<ParsedLogEvent> findByRecentEvents(Instant from, Instant to) {
        return repository.findByTimestampBetween(from, to);
    }

    public long count() {
        return repository.count();
    }
}
