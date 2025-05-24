package hawkeye.hawktrace.hawkcollector.parser;

import hawkeye.hawktrace.hawkcollector.model.LogEvent;
import hawkeye.hawktrace.hawkcollector.model.ParsedLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LogParseFactory {

    private final List<LogParser> parsers;

    public LogParseFactory(List<LogParser> parsers) {
        this.parsers = parsers;

        //Make sure they are ordered by priority
        this.parsers.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));

        log.info("Initialized LogParseFactory with {} parsers: {}", parsers.size(),
                parsers.stream().map(LogParser::getParserType).toList());
    }

    public ParsedLogEvent parseLogEvent(String rawLog) {
        for (LogParser parser : parsers) {
            if (parser.canParse(rawLog)) {
                log.debug("Using parser {} for log: {}", parser.getParserType(), rawLog);
                return parser.parse(rawLog);
            }
        }
        log.debug("No parser found for log: {}", rawLog);
        return createGenericEvent(rawLog);
    }

    private ParsedLogEvent createGenericEvent(String rawLog) {
        return ParsedLogEvent.builder()
                .rawContent(rawLog)
                .parserType("generic")
                .message(rawLog)
                .severity("INFO").build();
    }
}
