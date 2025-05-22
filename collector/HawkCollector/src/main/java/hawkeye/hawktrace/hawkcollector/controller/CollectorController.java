package hawkeye.hawktrace.hawkcollector.controller;

import hawkeye.hawktrace.hawkcollector.service.FileLogCollectorService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/collector")
@RequiredArgsConstructor
public class CollectorController {

    private final FileLogCollectorService fileLogCollectorService;

    @PostMapping("/file/start")
    public ResponseEntity<Map<String, String>> startFileCollection(
            @RequestParam String filePath,
            @RequestParam(defaultValue = "generic") String logType
    ) {
        fileLogCollectorService.startCollecting(filePath, logType);
        return ResponseEntity.ok(Map.of(
                "status", "started",
                "file", filePath,
                "type", logType
        ));
    }

    @PostMapping("/file/stop")
    public ResponseEntity<Map<String, String>> stopFileCollection() {
        fileLogCollectorService.stopCollecting();
        return ResponseEntity.ok(Map.of(
                "status", "stopped"
        ));
    }

    @GetMapping("/file/status")
    public ResponseEntity<Map<String, Boolean>> getFileCollectionStatus() {
        return ResponseEntity.ok(Map.of(
                "running", fileLogCollectorService.isRunning()
        ));
    }
}
