package backend_instituciones.backend_instituciones.dto.response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ImportJobResponse {
    private String jobId;
    private String type;
    private String status;
    private Integer processed;
    private Integer created;
    private Integer updated;
    private Integer failed;
    private List<Map<String, Object>> errors;
    private LocalDateTime createdAt;
}
