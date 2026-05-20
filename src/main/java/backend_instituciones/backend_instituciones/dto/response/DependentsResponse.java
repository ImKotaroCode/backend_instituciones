package backend_instituciones.backend_instituciones.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DependentsResponse {
    private Long userId;
    private List<Long> studentIds;
    private List<StudentInfo> students;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StudentInfo {
        private Long id;
        private String name;
        private String documentNumber;
    }
}
