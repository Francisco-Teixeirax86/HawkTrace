package hawkeye.hawktrace.hawkcollector.parser;

import hawkeye.hawktrace.hawkcollector.model.ParsedLogEvent;

public interface LogParser {

    /**
     * Check if this parser can handle the given log line
     */
    boolean canParse(String line);

    /**
     * Parse the log line into a structured event
     */
    ParsedLogEvent parse(String rawline);

    /**
     * Get the type of this parser
     */
    String getParserType();

    /**
     * Get the priority of this parser (lower number = higher priority)
     */
    default int getPriority() {
        return 100;
    }

}
