package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class SupabaseStorageService {

    private final RestClient restClient;
    private final String apiUrl;
    private final String serviceKey;
    private final String bucket;

    public SupabaseStorageService(
            @Value("${supabase.api-url}") String apiUrl,
            @Value("${supabase.service-key}") String serviceKey,
            @Value("${supabase.bucket}") String bucket) {
        this.apiUrl = apiUrl;
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.restClient = RestClient.create();
    }

    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required", HttpStatus.BAD_REQUEST, "FILE_REQUIRED");
        }

        String ext = getExtension(file.getOriginalFilename());
        String path = folder + "/" + UUID.randomUUID() + ext;
        String uploadUrl = apiUrl + "/storage/v1/object/" + bucket + "/" + path;

        try {
            byte[] bytes = file.getBytes();
            MediaType contentType = resolveContentType(file.getContentType());

            restClient.put()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + serviceKey)
                    .contentType(contentType)
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();

            String publicUrl = apiUrl + "/storage/v1/object/public/" + bucket + "/" + path;
            log.info("File uploaded to Supabase: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            throw new BusinessException("Failed to read file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, "FILE_READ_ERROR");
        } catch (Exception e) {
            log.error("Supabase upload failed: {}", e.getMessage());
            throw new BusinessException("Upload failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, "UPLOAD_FAILED");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    private MediaType resolveContentType(String contentType) {
        if (contentType == null) return MediaType.APPLICATION_OCTET_STREAM;
        return MediaType.parseMediaType(contentType);
    }
}
