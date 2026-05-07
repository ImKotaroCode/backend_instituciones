package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BrandingResponse {
    private String name;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String backgroundImage;
    private String fontFamily;
}
