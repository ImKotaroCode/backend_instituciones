package backend_instituciones.backend_instituciones.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCenterSearchResponse {

    private String query;
    private int total;
    private List<PersonItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonItem {
        private Long id;
        private String role;
        private String name;
        private String email;
        private String documentNumber;
        private boolean active;
    }
}
