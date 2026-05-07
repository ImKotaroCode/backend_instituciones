package backend_instituciones.backend_instituciones.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BrandingRequest {
    @NotBlank
    private String name;
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid hex color")
    private String primaryColor;
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid hex color")
    private String secondaryColor;
    private String backgroundImage;
    private String logoUrl;
    private String fontFamily;
}
