package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmissionFileResponse {

    private Long id;
    private String name;
    private String url;
    private String mimeType;
    private Long sizeBytes;
    private String previewUrl;
}
