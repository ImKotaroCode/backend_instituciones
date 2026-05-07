package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.service.SseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SseController {

    private final SseService sseService;

    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notifications(@RequestParam String token, HttpServletResponse response) {
        setSseHeaders(response);

        if (!sseService.isValid(token)) {
            throw new BusinessException("Invalid token", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        String userId = sseService.extractUserId(token);
        return sseService.subscribe(userId);
    }

    @GetMapping(value = "/grades/{courseId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter grades(
            @PathVariable Long courseId,
            @RequestParam String token,
            HttpServletResponse response) {
        setSseHeaders(response);

        if (!sseService.isValid(token)) {
            throw new BusinessException("Invalid token", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        String userId = sseService.extractUserId(token);
        return sseService.subscribe("grades-" + courseId + "-" + userId);
    }

    @GetMapping(value = "/attendance/{courseId}/{date}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter attendance(
            @PathVariable Long courseId,
            @PathVariable LocalDate date,
            @RequestParam String token,
            HttpServletResponse response) {
        setSseHeaders(response);

        if (!sseService.isValid(token)) {
            throw new BusinessException("Invalid token", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        String userId = sseService.extractUserId(token);
        return sseService.subscribe("attendance-" + courseId + "-" + date + "-" + userId);
    }

    @GetMapping(value = "/export-progress/{jobId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter exportProgress(
            @PathVariable Long jobId,
            @RequestParam String token,
            HttpServletResponse response) {
        setSseHeaders(response);

        if (!sseService.isValid(token)) {
            throw new BusinessException("Invalid token", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        String userId = sseService.extractUserId(token);
        return sseService.subscribe("export-" + jobId + "-" + userId);
    }

    private void setSseHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
    }
}