package backend_instituciones.backend_instituciones.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_signatures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseSignature {

    /** PK = userId (one signature per user) */
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "signer_name", length = 300)
    private String signerName;

    @Column(name = "signature_url", length = 1000)
    private String signatureUrl;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
