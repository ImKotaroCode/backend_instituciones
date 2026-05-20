package backend_instituciones.backend_instituciones.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonSummaryResponse {
    private String personType;
    private Long personId;
    private int presentCount;
    private int lateCount;
    private int absentCount;
    // Teacher only
    private Integer totalTardinessMinutes;
    // Student only
    private Integer excusedCount;
}
