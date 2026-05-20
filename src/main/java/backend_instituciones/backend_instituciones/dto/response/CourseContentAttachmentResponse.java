package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseContentAttachmentResponse {
    private Long id;
    private String name;
    private String url;
    private String mimeType;
    private Long sizeBytes;
    private String previewUrl;
}
