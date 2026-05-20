package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.service.SseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SseController {

    private final SseService sseService;

    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notifications(@RequestParam String token, HttpServletResponse response) {
        setSseHeaders(response);
        if (!sseService.isValid(token)) return errorEmitter("INVALID_TOKEN");
        String userId = sseService.extractUserId(token);
        if (userId == null) return errorEmitter("USER_NOT_FOUND");
        return sseService.subscribe(userId);
    }

    @GetMapping(value = "/grades/{courseId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter grades(
            @PathVariable Long courseId,
            @RequestParam String token,
            HttpServletResponse response) {
        setSseHeaders(response);
        if (!sseService.isValid(token)) return errorEmitter("INVALID_TOKEN");
        String userId = sseService.extractUserId(token);
        if (userId == null) return errorEmitter("USER_NOT_FOUND");
        return sseService.subscribe("grades-" + courseId + "-" + userId);
    }

    @GetMapping(value = "/attendance/{courseId}/{date}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter attendance(
            @PathVariable Long courseId,
            @PathVariable LocalDate date,
            @RequestParam String token,
            HttpServletResponse response) {
        setSseHeaders(response);
        if (!sseService.isValid(token)) return errorEmitter("INVALID_TOKEN");
        String userId = sseService.extractUserId(token);
        if (userId == null) return errorEmitter("USER_NOT_FOUND");
        return sseService.subscribe("attendance-" + courseId + "-" + date + "-" + userId);
    }

    @GetMapping(value = "/export-progress/{jobId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter exportProgress(
            @PathVariable Long jobId,
            @RequestParam String token,
            HttpServletResponse response) {
        setSseHeaders(response);
        if (!sseService.isValid(token)) return errorEmitter("INVALID_TOKEN");
        String userId = sseService.extractUserId(token);
        if (userId == null) return errorEmitter("USER_NOT_FOUND");
        return sseService.subscribe("export-" + jobId + "-" + userId);
    }

    private SseEmitter errorEmitter(String errorCode) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Map.of("error", errorCode)));
        } catch (IOException ignored) {}
        emitter.complete();
        return emitter;
    }

    private void setSseHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
    }
}