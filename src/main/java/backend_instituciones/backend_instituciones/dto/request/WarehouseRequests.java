package backend_instituciones.backend_instituciones.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Namespace class — all warehouse request DTOs in one file for brevity. */
public class WarehouseRequests {

    @Data public static class ExportRequest {
        private String dataset;   // ACTIVOS, CATEGORIAS, ESPACIOS, MOVIMIENTOS, ...
        private String format;    // PDF, XLSX, CSV
        private String title;
        private String groupBy;
        private List<String> fields;
        private Map<String, Object> filters;
    }

    @Data public static class SectorRequest {
        private String name;
        private String code;
        private String campusName;
        private String notes;
    }

    @Data public static class PavilionRequest {
        private Long sectorId;
        private String sectorName;
        private String name;
        private String code;
        private Integer floorCount;
        private String notes;
    }

    @Data public static class RoomRequest {
        private Long sectorId;
        private Long pavilionId;
        private String sectorName;
        private String pavilionName;
        private String name;
        private String code;
        private String roomType;
        private String floor;
        private Integer capacity;
        private Integer aforo;
        private Long teacherResponsibleId;
        private String teacherResponsibleName;
        private String notes;
    }

    @Data public static class AssetCategoryRequest {
        private String mainCategory;
        private String name;
        private String description;
        private Boolean active;
    }

    @Data public static class MovementRequest {
        private Long assetId;
        private String assetCode;
        private String assetName;
        private String type;
        private LocalDateTime occurredAt;
        private Long originRoomId;
        private String originLabel;
        private Long destinationRoomId;
        private String destinationLabel;
        private Long responsibleUserId;
        private String responsibleUserName;
        private Long signedByUserId;
        private String signedByUserName;
        private String signatureUrl;
        private String supportNumber;
        private String notes;
    }

    @Data public static class MaintenanceRequest {
        private Long assetId;
        private String assetCode;
        private String assetName;
        private String type;
        private String status;
        private LocalDate scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
        private String providerName;
        private String technicianName;
        private BigDecimal cost;
        private LocalDate warrantyUntil;
        private Long responsibleUserId;
        private String responsibleUserName;
        private Long signedByUserId;
        private String signedByUserName;
        private String signatureUrl;
        private String notes;
    }

    @Data public static class LoanRequest {
        private Long assetId;
        private String assetCode;
        private String assetName;
        private String requesterRole;
        private Long requesterId;
        private String requesterName;
        private LocalDateTime outAt;
        private LocalDateTime dueAt;
        private String deliveryCondition;
        private Long responsibleUserId;
        private String responsibleUserName;
        private Long signedByUserId;
        private String signedByUserName;
        private String signatureUrl;
        private String notes;
    }

    @Data public static class LoanReturnRequest {
        private LocalDateTime returnedAt;
        private String returnCondition;
        private String penaltyNotes;
    }

    @Data public static class SupplierRequest {
        private String name;
        private String ruc;
        private String contactName;
        private String phone;
        private String email;
        private String address;
        private String notes;
    }

    @Data public static class PurchaseOrderRequest {
        private String orderNumber;
        private String title;
        private String itemDescription;
        private Long categoryId;
        private String categoryName;
        private Integer quantity;
        private String urgency;
        private Long requestedByUserId;
        private String requestedByUserName;
        private LocalDate requestedAt;
        private String status;
        private Long supplierId;
        private String supplierName;
        private String approvalNote;
        private String documentUrl;
        private BigDecimal totalAmount;
        private String notes;
    }

    @Data public static class PurchaseQuotationRequest {
        private Long purchaseOrderId;
        private Long supplierId;
        private String supplierName;
        private LocalDate quotedAt;
        private BigDecimal amount;
        private String notes;
        // file handled in controller as MultipartFile
    }

    @Data public static class PurchaseIncidentRequest {
        private Long purchaseOrderId;
        private LocalDate reportedAt;
        private String title;
        private String detail;
        private String status;
    }

    @Data public static class InventoryRequest {
        private Long roomId;
        private String roomName;
        private LocalDateTime countedAt;
        private Integer expectedCount;
        private Integer physicalCount;
        private Long responsibleUserId;
        private String responsibleUserName;
        private Long signedByUserId;
        private String signedByUserName;
        private String signatureUrl;
        private String notes;
    }
}
