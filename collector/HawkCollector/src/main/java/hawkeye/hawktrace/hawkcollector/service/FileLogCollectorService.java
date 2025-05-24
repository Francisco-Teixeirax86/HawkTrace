package hawkeye.hawktrace.hawkcollector.service;

import hawkeye.hawktrace.hawkcollector.model.LogEvent;
import hawkeye.hawktrace.hawkcollector.model.ParsedLogEvent;
import hawkeye.hawktrace.hawkcollector.parser.LogParseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class FileLogCollectorService {

    private final KafkaTemplate<String, LogEvent> kafkaTemplate;
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String hostname;
    private final LogParseFactory logParseFactory;


    @Value("${hawktrace.kafka.topic.logs}")
    private String logsTopic;

    @Autowired
    public FileLogCollectorService(KafkaTemplate<String, LogEvent> kafkaTemplate, LogParseFactory logParseFactory) {
        this.kafkaTemplate = kafkaTemplate;
        this.executorService = Executors.newSingleThreadExecutor();
        this.logParseFactory = logParseFactory;

        String hostnameTemp;
        //Get HostName for log events
        try {
            hostnameTemp = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostnameTemp = "unknown";
            log.warn("Could not determine hostname", e);
        }

        this.hostname = hostnameTemp;
    }

    public void startCollecting(String logFilePath, String logType) {
        if (running.get()) {
            log.warn("File collector already running");
            return;
        }

        running.set(true);
        executorService.submit(() -> {
            try {
                Path path = Paths.get(logFilePath);

                if (!Files.exists(path)) {
                    log.error("Log file does not exist: {}", logFilePath);
                    running.set(false);
                    return;
                }

                //Create a watch service
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path dir = path.getParent();
                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                long filePosition = Files.size(path);
                log.info("Starting collection from position {} in file {}", filePosition, logFilePath);

                while (running.get()) {
                    //Check for new content
                    long newLength = Files.size(path);
                    if (newLength > filePosition) {
                        byte[] bytes = Files.readAllBytes(path);
                        String newContent = new String(bytes, (int) filePosition, (int) (newLength - filePosition));

                        //Process each line
                        for(String line: newContent.split("\n")) {
                            if(!line.trim().isEmpty()) {
                                //Create and send log event to kafka
                                LogEvent logEvent = LogEvent.builder()
                                        .id(UUID.randomUUID().toString())
                                        .source(logFilePath)
                                        .rawContent(line)
                                        .timestamp(Instant.now())
                                        .logType(logType)
                                        .hostName(hostname)
                                        .build();

                                kafkaTemplate.send(logsTopic, logEvent.getId(), logEvent);
                                log.debug("Sent log event to kafka: {}", logEvent.getId());

                                //Parse and process with the custom parsers
                                ParsedLogEvent parsedLogEvent = logParseFactory.parseLogEvent(line);
                                parsedLogEvent.setSource(logFilePath);
                                parsedLogEvent.setLogType(logType);
                                parsedLogEvent.setHostName(hostname);

                                log.debug("Parsed log with parser '{}': {}", parsedLogEvent.getParserType(), parsedLogEvent.getId());
                            }
                        }

                        filePosition = newLength;
                    }

                    //Wait for file changes
                    WatchKey key = watchService.take();
                    key.pollEvents();
                    key.reset();
                }
            } catch (Exception e) {
                log.error("Error occurred while collecting", e);
                running.set(false);
            }

        });

        log.info("Started collecting logs from {} with type {}", logFilePath, logType);
    }

    public void stopCollecting() {
        running.set(false);
        log.info("Stopped collecting logs");
    }

    public boolean isRunning() {
        return running.get();
    }
}
