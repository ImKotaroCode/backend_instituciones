package backend_instituciones.backend_instituciones.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentChildResponse {

    private Long id;
    private String name;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private String documentNumber;
    private String studentCode;   // null until entity has code field
    private Long levelId;
    private Long gradeId;
    private Long sectionId;
    private String levelName;
    private String gradeName;
    private String sectionName;
    private String status;        // ACTIVE | INACTIVE
}
