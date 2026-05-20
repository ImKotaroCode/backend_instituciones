package backend_instituciones.backend_instituciones.controller;

import backend_instituciones.backend_instituciones.dto.request.WarehouseRequests.*;
import backend_instituciones.backend_instituciones.security.TenantContext;
import backend_instituciones.backend_instituciones.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'ALMACEN','ADMINISTRACION')")
public class WarehouseController {

    private final WarehouseService warehouseService;

    // ── Sectors ───────────────────────────────────────────────────────────────

    @GetMapping("/sectors")
    public ResponseEntity<?> listSectors() {
        return ResponseEntity.ok(
                warehouseService.listSectors(TenantContext.getInstitutionId()));
    }

    @PostMapping("/sectors")
    public ResponseEntity<?> createSector(@RequestBody SectorRequest req) {
        return ResponseEntity.ok(
                warehouseService.createSector(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/sectors/{id}")
    public ResponseEntity<?> updateSector(@PathVariable Long id,
                                          @RequestBody SectorRequest req) {
        return ResponseEntity.ok(
                warehouseService.updateSector(id, TenantContext.getInstitutionId(), req));
    }

    // ── Pavilions ─────────────────────────────────────────────────────────────

    @GetMapping("/pavilions")
    public ResponseEntity<?> listPavilions(
            @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(
                warehouseService.listPavilions(TenantContext.getInstitutionId(), sectorId));
    }

    @PostMapping("/pavilions")
    public ResponseEntity<?> createPavilion(@RequestBody PavilionRequest req) {
        return ResponseEntity.ok(
                warehouseService.createPavilion(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/pavilions/{id}")
    public ResponseEntity<?> updatePavilion(@PathVariable Long id,
                                             @RequestBody PavilionRequest req) {
        return ResponseEntity.ok(
                warehouseService.updatePavilion(id, TenantContext.getInstitutionId(), req));
    }

    // ── Rooms ─────────────────────────────────────────────────────────────────

    @GetMapping("/rooms")
    public ResponseEntity<?> listRooms(
            @RequestParam(required = false) Long sectorId,
            @RequestParam(required = false) Long pavilionId) {
        return ResponseEntity.ok(
                warehouseService.listRooms(TenantContext.getInstitutionId(), sectorId, pavilionId));
    }

    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest req) {
        return ResponseEntity.ok(
                warehouseService.createRoom(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id,
                                        @RequestBody RoomRequest req) {
        return ResponseEntity.ok(
                warehouseService.updateRoom(id, TenantContext.getInstitutionId(), req));
    }

    // ── Asset Categories ──────────────────────────────────────────────────────

    @GetMapping("/asset-categories")
    public ResponseEntity<?> listAssetCategories() {
        return ResponseEntity.ok(
                warehouseService.listAssetCategories(TenantContext.getInstitutionId()));
    }

    @PostMapping("/asset-categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> createAssetCategory(@RequestBody AssetCategoryRequest req) {
        return ResponseEntity.ok(
                warehouseService.createAssetCategory(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/asset-categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> updateAssetCategory(@PathVariable Long id,
                                                  @RequestBody AssetCategoryRequest req) {
        return ResponseEntity.ok(
                warehouseService.updateAssetCategory(id, TenantContext.getInstitutionId(), req));
    }

    // ── Assets ────────────────────────────────────────────────────────────────

    @GetMapping("/assets")
    public ResponseEntity<?> listAssets(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(
                warehouseService.listAssets(TenantContext.getInstitutionId(),
                        roomId, categoryId, status, search));
    }

    @GetMapping("/assets/{id}")
    public ResponseEntity<?> getAsset(@PathVariable Long id) {
        return ResponseEntity.ok(
                warehouseService.getAsset(id, TenantContext.getInstitutionId()));
    }

    @PostMapping(value = "/assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAsset(
            @RequestParam String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String mainCategory,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String acquisitionDate,
            @RequestParam(required = false) String initialValue,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) String pavilionName,
            @RequestParam(required = false) String sectorName,
            @RequestParam(required = false) Long teacherResponsibleId,
            @RequestParam(required = false) String teacherResponsibleName,
            @RequestParam(required = false) String warrantyUntil,
            @RequestParam(required = false) String reportedByName,
            @RequestParam(required = false) String reportedAt,
            @RequestParam(required = false) String reportObservation,
            @RequestParam(required = false) String pendingAction,
            @RequestParam(required = false) String notes,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(
                warehouseService.createAsset(TenantContext.getInstitutionId(),
                        name, categoryId, category, mainCategory, brand, model, serialNumber,
                        color, status, acquisitionDate, initialValue, roomId, roomName,
                        pavilionName, sectorName, teacherResponsibleId, teacherResponsibleName,
                        warrantyUntil, reportedByName, reportedAt, reportObservation,
                        pendingAction, notes, file));
    }

    @PutMapping(value = "/assets/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAsset(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String mainCategory,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String acquisitionDate,
            @RequestParam(required = false) String initialValue,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) String pavilionName,
            @RequestParam(required = false) String sectorName,
            @RequestParam(required = false) Long teacherResponsibleId,
            @RequestParam(required = false) String teacherResponsibleName,
            @RequestParam(required = false) String warrantyUntil,
            @RequestParam(required = false) String reportedByName,
            @RequestParam(required = false) String reportedAt,
            @RequestParam(required = false) String reportObservation,
            @RequestParam(required = false) String pendingAction,
            @RequestParam(required = false) String notes,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(
                warehouseService.updateAsset(id, TenantContext.getInstitutionId(),
                        name, categoryId, category, mainCategory, brand, model, serialNumber,
                        color, status, acquisitionDate, initialValue, roomId, roomName,
                        pavilionName, sectorName, teacherResponsibleId, teacherResponsibleName,
                        warrantyUntil, reportedByName, reportedAt, reportObservation,
                        pendingAction, notes, file));
    }

    @PostMapping(value = "/assets/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> bulkCreateAssets(
            @RequestParam String name,
            @RequestParam Integer quantity,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String mainCategory,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String acquisitionDate,
            @RequestParam(required = false) String initialValue,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) String pavilionName,
            @RequestParam(required = false) String sectorName,
            @RequestParam(required = false) Long teacherResponsibleId,
            @RequestParam(required = false) String teacherResponsibleName,
            @RequestParam(required = false) String warrantyUntil,
            @RequestParam(required = false) String notes,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(
                warehouseService.bulkCreateAssets(TenantContext.getInstitutionId(),
                        categoryId, category, mainCategory, name, quantity,
                        brand, model, color, status, acquisitionDate, initialValue,
                        roomId, roomName, pavilionName, sectorName,
                        teacherResponsibleId, teacherResponsibleName,
                        warrantyUntil, notes, file));
    }

    @GetMapping("/assets/{id}/relocations")
    public ResponseEntity<?> getRelocations(@PathVariable Long id) {
        return ResponseEntity.ok(
                warehouseService.getRelocations(id, TenantContext.getInstitutionId()));
    }

    // ── Movements ─────────────────────────────────────────────────────────────

    @GetMapping("/movements")
    public ResponseEntity<?> listMovements(
            @RequestParam(required = false) Long assetId) {
        return ResponseEntity.ok(
                warehouseService.listMovements(TenantContext.getInstitutionId(), assetId));
    }

    @PostMapping("/movements")
    public ResponseEntity<?> createMovement(@RequestBody MovementRequest req) {
        return ResponseEntity.ok(
                warehouseService.createMovement(TenantContext.getInstitutionId(), req));
    }

    // ── Maintenance ───────────────────────────────────────────────────────────

    @GetMapping("/maintenance")
    public ResponseEntity<?> listMaintenance(
            @RequestParam(required = false) Long assetId) {
        return ResponseEntity.ok(
                warehouseService.listMaintenance(TenantContext.getInstitutionId(), assetId));
    }

    @PostMapping("/maintenance")
    public ResponseEntity<?> createMaintenance(@RequestBody MaintenanceRequest req) {
        return ResponseEntity.ok(
                warehouseService.createMaintenance(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/maintenance/{id}")
    public ResponseEntity<?> updateMaintenance(@PathVariable Long id,
                                               @RequestBody MaintenanceRequest req) {
        return ResponseEntity.ok(
                warehouseService.updateMaintenance(id, TenantContext.getInstitutionId(), req));
    }

    // ── Loans ─────────────────────────────────────────────────────────────────

    @GetMapping("/loans")
    public ResponseEntity<?> listLoans(
            @RequestParam(required = false) Long assetId) {
        return ResponseEntity.ok(
                warehouseService.listLoans(TenantContext.getInstitutionId(), assetId));
    }

    @PostMapping("/loans")
    public ResponseEntity<?> createLoan(@RequestBody LoanRequest req) {
        return ResponseEntity.ok(
                warehouseService.createLoan(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/loans/{id}")
    public ResponseEntity<?> updateLoan(@PathVariable Long id,
                                        @RequestBody LoanRequest req) {
        return ResponseEntity.ok(
                warehouseService.updateLoan(id, TenantContext.getInstitutionId(), req));
    }

    @PatchMapping("/loans/{id}/return")
    public ResponseEntity<?> returnLoan(@PathVariable Long id,
                                        @RequestBody LoanReturnRequest req) {
        return ResponseEntity.ok(
                warehouseService.returnLoan(id, TenantContext.getInstitutionId(), req));
    }

    // ── Suppliers ─────────────────────────────────────────────────────────────

    @GetMapping("/suppliers")
    public ResponseEntity<?> listSuppliers() {
        return ResponseEntity.ok(
                warehouseService.listSuppliers(TenantContext.getInstitutionId()));
    }

    @PostMapping("/suppliers")
    public ResponseEntity<?> createSupplier(@RequestBody SupplierRequest req) {
        return ResponseEntity.ok(
                warehouseService.createSupplier(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<?> updateSupplier(@PathVariable Long id,
                                            @RequestBody SupplierRequest req) {
        return ResponseEntity.ok(
                warehouseService.updateSupplier(id, TenantContext.getInstitutionId(), req));
    }

    // ── Purchase Orders ───────────────────────────────────────────────────────

    @GetMapping("/purchase-orders")
    public ResponseEntity<?> listPurchaseOrders() {
        return ResponseEntity.ok(
                warehouseService.listPurchaseOrders(TenantContext.getInstitutionId()));
    }

    @PostMapping("/purchase-orders")
    public ResponseEntity<?> createPurchaseOrder(@RequestBody PurchaseOrderRequest req) {
        return ResponseEntity.ok(
                warehouseService.createPurchaseOrder(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/purchase-orders/{id}")
    public ResponseEntity<?> updatePurchaseOrder(@PathVariable Long id,
                                                  @RequestBody PurchaseOrderRequest req) {
        return ResponseEntity.ok(
                warehouseService.updatePurchaseOrder(id, TenantContext.getInstitutionId(),
                        req, hasAdminRole()));
    }

    // ── Purchase Quotations ───────────────────────────────────────────────────

    @GetMapping("/purchase-quotations")
    public ResponseEntity<?> listPurchaseQuotations(
            @RequestParam(required = false) Long purchaseOrderId) {
        return ResponseEntity.ok(
                warehouseService.listPurchaseQuotations(purchaseOrderId, TenantContext.getInstitutionId()));
    }

    @GetMapping("/purchase-orders/{orderId}/quotations")
    public ResponseEntity<?> listPurchaseQuotationsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                warehouseService.listPurchaseQuotations(orderId, TenantContext.getInstitutionId()));
    }

    @PostMapping(value = "/purchase-quotations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPurchaseQuotation(
            @RequestParam Long purchaseOrderId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String quotedAt,
            @RequestParam(required = false) String amount,
            @RequestParam(required = false) String notes,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        PurchaseQuotationRequest req = new PurchaseQuotationRequest();
        req.setPurchaseOrderId(purchaseOrderId);
        req.setSupplierId(supplierId);
        req.setSupplierName(supplierName);
        req.setQuotedAt(quotedAt != null ? java.time.LocalDate.parse(quotedAt) : null);
        req.setAmount(amount != null ? new java.math.BigDecimal(amount) : null);
        req.setNotes(notes);
        return ResponseEntity.ok(
                warehouseService.createPurchaseQuotation(TenantContext.getInstitutionId(), req, file));
    }

    @PutMapping("/purchase-quotations/{id}")
    public ResponseEntity<?> updatePurchaseQuotation(@PathVariable Long id,
                                                      @RequestBody PurchaseQuotationRequest req) {
        return ResponseEntity.ok(
                warehouseService.updatePurchaseQuotation(id, TenantContext.getInstitutionId(), req));
    }

    @PatchMapping("/purchase-quotations/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR','ADMINISTRACION')")
    public ResponseEntity<?> approveQuotation(@PathVariable Long id) {
        return ResponseEntity.ok(
                warehouseService.approveQuotation(id, TenantContext.getInstitutionId()));
    }

    // ── Purchase Incidents ────────────────────────────────────────────────────

    @GetMapping("/purchase-incidents")
    public ResponseEntity<?> listPurchaseIncidents(
            @RequestParam(required = false) Long purchaseOrderId) {
        return ResponseEntity.ok(
                warehouseService.listPurchaseIncidents(purchaseOrderId, TenantContext.getInstitutionId()));
    }

    @GetMapping("/purchase-orders/{orderId}/incidents")
    public ResponseEntity<?> listPurchaseIncidentsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                warehouseService.listPurchaseIncidents(orderId, TenantContext.getInstitutionId()));
    }

    @PostMapping("/purchase-incidents")
    public ResponseEntity<?> createPurchaseIncident(@RequestBody PurchaseIncidentRequest req) {
        return ResponseEntity.ok(
                warehouseService.createPurchaseIncident(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/purchase-incidents/{id}")
    public ResponseEntity<?> updatePurchaseIncident(@PathVariable Long id,
                                                     @RequestBody PurchaseIncidentRequest req) {
        return ResponseEntity.ok(
                warehouseService.updatePurchaseIncident(id, TenantContext.getInstitutionId(), req));
    }

    // ── Inventories ───────────────────────────────────────────────────────────

    @GetMapping("/inventories")
    public ResponseEntity<?> listInventories() {
        return ResponseEntity.ok(
                warehouseService.listInventories(TenantContext.getInstitutionId()));
    }

    @PostMapping("/inventories")
    public ResponseEntity<?> createInventory(@RequestBody InventoryRequest req) {
        return ResponseEntity.ok(
                warehouseService.createInventory(TenantContext.getInstitutionId(), req));
    }

    @PutMapping("/inventories/{id}")
    public ResponseEntity<?> updateInventory(@PathVariable Long id,
                                             @RequestBody InventoryRequest req) {
        return ResponseEntity.ok(
                warehouseService.updateInventory(id, TenantContext.getInstitutionId(), req));
    }

    // ── Signatures ────────────────────────────────────────────────────────────

    @GetMapping("/signatures/{userId}")
    public ResponseEntity<?> getSignature(@PathVariable Long userId) {
        return ResponseEntity.ok(
                warehouseService.getSignature(userId, TenantContext.getInstitutionId()));
    }

    @PostMapping(value = "/signatures/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveSignature(
            @PathVariable Long userId,
            @RequestParam(required = false) String signerName,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(
                warehouseService.saveSignature(userId, TenantContext.getInstitutionId(),
                        signerName, file));
    }

    @PostMapping(value = "/signatures", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveSignatureByParam(
            @RequestParam Long userId,
            @RequestParam(required = false) String signerName,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(
                warehouseService.saveSignature(userId, TenantContext.getInstitutionId(),
                        signerName, file));
    }

    // ── Alerts ────────────────────────────────────────────────────────────────

    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts() {
        return ResponseEntity.ok(
                warehouseService.getAlerts(TenantContext.getInstitutionId()));
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    @GetMapping("/reports/summary")
    public ResponseEntity<?> getReportSummary() {
        return ResponseEntity.ok(
                warehouseService.getReportSummary(TenantContext.getInstitutionId()));
    }

    @PostMapping("/reports/export")
    public ResponseEntity<byte[]> exportReport(@RequestBody ExportRequest req) {
        WarehouseService.ExportResult result = warehouseService.exportReport(
                TenantContext.getInstitutionId(), req, hasAdminRole());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.contentType()));
        headers.setContentDispositionFormData("attachment", result.filename());
        return ResponseEntity.ok().headers(headers).body(result.bytes());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean hasAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_DIRECTOR".equals(a.getAuthority()));
    }
}
