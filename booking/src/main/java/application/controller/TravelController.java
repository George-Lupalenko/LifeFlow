package application.controller;

import application.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/travel")
@CrossOrigin("*")
public class TravelController {

    private final AiService aiService;

    @PostMapping("/search")
    public ResponseEntity<Map<String,String>> search(@RequestBody UserQuery request) {
        String response = aiService.generateTravelResponse(request.query());
        log.info(response);
        return ResponseEntity.ok(Map.of("aiRecommendation", response));
    }

    public record UserQuery(String query) {}
}
