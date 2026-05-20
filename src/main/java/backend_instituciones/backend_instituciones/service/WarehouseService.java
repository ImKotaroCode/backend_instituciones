package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.dto.request.WarehouseRequests.*;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WarehouseService {

    private final InstitutionConfigRepository institutionConfigRepo;
    private final WarehouseSectorRepository sectorRepo;
    private final WarehousePavilionRepository pavilionRepo;
    private final WarehouseRoomRepository roomRepo;
    private final WarehouseAssetCategoryRepository categoryRepo;
    private final WarehouseAssetRepository assetRepo;
    private final WarehouseMovementRepository movementRepo;
    private final WarehouseMaintenanceRepository maintenanceRepo;
    private final WarehouseLoanRepository loanRepo;
    private final WarehouseSupplierRepository supplierRepo;
    private final WarehousePurchaseOrderRepository purchaseOrderRepo;
    private final WarehousePurchaseQuotationRepository quotationRepo;
    private final WarehousePurchaseIncidentRepository incidentRepo;
    private final WarehouseInventoryRepository inventoryRepo;
    private final WarehouseSignatureRepository signatureRepo;
    private final SupabaseStorageService storageService;

    // ── 1. Sectors ────────────────────────────────────────────────────────────

    public List<Map<String, Object>> listSectors(Long institutionId) {
        return sectorRepo.findByInstitutionIdOrderByNameAsc(institutionId)
                .stream().map(this::sectorMap).toList();
    }

    @Transactional
    public Map<String, Object> createSector(Long institutionId, SectorRequest req) {
        WarehouseSector s = sectorRepo.save(WarehouseSector.builder()
                .institutionId(institutionId)
                .name(req.getName())
                .code(req.getCode())
                .campusName(req.getCampusName())
                .notes(req.getNotes())
                .build());
        return sectorMap(s);
    }

    @Transactional
    public Map<String, Object> updateSector(Long id, Long institutionId, SectorRequest req) {
        WarehouseSector s = sectorRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseSector", id));
        s.setName(req.getName());
        s.setCode(req.getCode());
        s.setCampusName(req.getCampusName());
        s.setNotes(req.getNotes());
        return sectorMap(sectorRepo.save(s));
    }

    // ── 2. Pavilions ──────────────────────────────────────────────────────────

    public List<Map<String, Object>> listPavilions(Long institutionId, Long sectorId) {
        List<WarehousePavilion> list = sectorId != null
                ? pavilionRepo.findByInstitutionIdAndSectorIdOrderByNameAsc(institutionId, sectorId)
                : pavilionRepo.findByInstitutionIdOrderByNameAsc(institutionId);
        return list.stream().map(this::pavilionMap).toList();
    }

    @Transactional
    public Map<String, Object> createPavilion(Long institutionId, PavilionRequest req) {
        WarehousePavilion p = pavilionRepo.save(WarehousePavilion.builder()
                .institutionId(institutionId)
                .sectorId(req.getSectorId())
                .sectorName(req.getSectorName())
                .name(req.getName())
                .code(req.getCode())
                .floorCount(req.getFloorCount())
                .notes(req.getNotes())
                .build());
        return pavilionMap(p);
    }

    @Transactional
    public Map<String, Object> updatePavilion(Long id, Long institutionId, PavilionRequest req) {
        WarehousePavilion p = pavilionRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehousePavilion", id));
        p.setSectorId(req.getSectorId());
        p.setSectorName(req.getSectorName());
        p.setName(req.getName());
        p.setCode(req.getCode());
        p.setFloorCount(req.getFloorCount());
        p.setNotes(req.getNotes());
        return pavilionMap(pavilionRepo.save(p));
    }

    // ── 3. Rooms ──────────────────────────────────────────────────────────────

    public List<Map<String, Object>> listRooms(Long institutionId, Long sectorId, Long pavilionId) {
        List<WarehouseRoom> list;
        if (sectorId != null && pavilionId != null)
            list = roomRepo.findByInstitutionIdAndSectorIdAndPavilionIdOrderByNameAsc(institutionId, sectorId, pavilionId);
        else if (sectorId != null)
            list = roomRepo.findByInstitutionIdAndSectorIdOrderByNameAsc(institutionId, sectorId);
        else if (pavilionId != null)
            list = roomRepo.findByInstitutionIdAndPavilionIdOrderByNameAsc(institutionId, pavilionId);
        else
            list = roomRepo.findByInstitutionIdOrderByNameAsc(institutionId);
        return list.stream().map(this::roomMap).toList();
    }

    @Transactional
    public Map<String, Object> createRoom(Long institutionId, RoomRequest req) {
        WarehouseRoom r = roomRepo.save(WarehouseRoom.builder()
                .institutionId(institutionId)
                .sectorId(req.getSectorId())
                .pavilionId(req.getPavilionId())
                .sectorName(req.getSectorName())
                .pavilionName(req.getPavilionName())
                .name(req.getName())
                .code(req.getCode())
                .roomType(req.getRoomType())
                .floor(req.getFloor())
                .capacity(req.getCapacity())
                .aforo(req.getAforo())
                .teacherResponsibleId(req.getTeacherResponsibleId())
                .teacherResponsibleName(req.getTeacherResponsibleName())
                .notes(req.getNotes())
                .build());
        return roomMap(r);
    }

    @Transactional
    public Map<String, Object> updateRoom(Long id, Long institutionId, RoomRequest req) {
        WarehouseRoom r = roomRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseRoom", id));
        r.setSectorId(req.getSectorId());
        r.setPavilionId(req.getPavilionId());
        r.setSectorName(req.getSectorName());
        r.setPavilionName(req.getPavilionName());
        r.setName(req.getName());
        r.setCode(req.getCode());
        r.setRoomType(req.getRoomType());
        r.setFloor(req.getFloor());
        r.setCapacity(req.getCapacity());
        r.setAforo(req.getAforo());
        r.setTeacherResponsibleId(req.getTeacherResponsibleId());
        r.setTeacherResponsibleName(req.getTeacherResponsibleName());
        r.setNotes(req.getNotes());
        return roomMap(roomRepo.save(r));
    }

    // ── 4. Asset Categories ───────────────────────────────────────────────────

    public List<Map<String, Object>> listAssetCategories(Long institutionId) {
        return categoryRepo.findByInstitutionIdOrderByMainCategoryAscNameAsc(institutionId)
                .stream().map(this::categoryMap).toList();
    }

    @Transactional
    public Map<String, Object> createAssetCategory(Long institutionId, AssetCategoryRequest req) {
        WarehouseAssetCategory c = categoryRepo.save(WarehouseAssetCategory.builder()
                .institutionId(institutionId)
                .mainCategory(req.getMainCategory())
                .name(req.getName())
                .description(req.getDescription())
                .active(req.getActive() != null ? req.getActive() : true)
                .build());
        return categoryMap(c);
    }

    @Transactional
    public Map<String, Object> updateAssetCategory(Long id, Long institutionId, AssetCategoryRequest req) {
        WarehouseAssetCategory c = categoryRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseAssetCategory", id));
        if (req.getMainCategory() != null) c.setMainCategory(req.getMainCategory());
        if (req.getName() != null) c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getActive() != null) c.setActive(req.getActive());
        return categoryMap(categoryRepo.save(c));
    }

    // ── 5. Assets ─────────────────────────────────────────────────────────────

    public List<Map<String, Object>> listAssets(Long institutionId, Long roomId,
                                                 Long categoryId, String status, String search) {
        return assetRepo.search(institutionId, roomId, categoryId, status, search)
                .stream().map(this::assetMap).toList();
    }

    public Map<String, Object> getAsset(Long id, Long institutionId) {
        return assetMap(assetRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseAsset", id)));
    }

    @Transactional
    public Map<String, Object> createAsset(Long institutionId,
                                            String name, Long categoryId, String category,
                                            String mainCategory, String brand, String model,
                                            String serialNumber, String color, String status,
                                            String acquisitionDate, String initialValue,
                                            Long roomId, String roomName, String pavilionName,
                                            String sectorName, Long teacherResponsibleId,
                                            String teacherResponsibleName, String warrantyUntil,
                                            String reportedByName, String reportedAt,
                                            String reportObservation, String pendingAction,
                                            String notes, MultipartFile file) {
        WarehouseAsset asset = WarehouseAsset.builder()
                .institutionId(institutionId)
                .name(name)
                .categoryId(categoryId)
                .category(category)
                .mainCategory(mainCategory)
                .brand(brand)
                .model(model)
                .serialNumber(serialNumber)
                .color(color)
                .status(status != null ? status : "BUENO")
                .acquisitionDate(acquisitionDate != null ? LocalDate.parse(acquisitionDate) : null)
                .initialValue(initialValue != null ? new java.math.BigDecimal(initialValue) : null)
                .roomId(roomId)
                .roomName(roomName)
                .pavilionName(pavilionName)
                .sectorName(sectorName)
                .teacherResponsibleId(teacherResponsibleId)
                .teacherResponsibleName(teacherResponsibleName)
                .warrantyUntil(warrantyUntil != null ? LocalDate.parse(warrantyUntil) : null)
                .reportedByName(reportedByName)
                .reportedAt(reportedAt != null ? LocalDate.parse(reportedAt) : null)
                .reportObservation(reportObservation)
                .pendingAction(pendingAction != null ? pendingAction : "NINGUNA")
                .notes(notes)
                .build();

        WarehouseAsset saved = assetRepo.save(asset);
        saved.setCode(generateAssetCode(category, saved.getId()));

        if (file != null && !file.isEmpty()) {
            saved.setPhotoUrl(storageService.upload(file, "assets"));
        }

        return assetMap(assetRepo.save(saved));
    }

    @Transactional
    public Map<String, Object> updateAsset(Long id, Long institutionId,
                                            String name, Long categoryId, String category,
                                            String mainCategory, String brand, String model,
                                            String serialNumber, String color, String status,
                                            String acquisitionDate, String initialValue,
                                            Long roomId, String roomName, String pavilionName,
                                            String sectorName, Long teacherResponsibleId,
                                            String teacherResponsibleName, String warrantyUntil,
                                            String reportedByName, String reportedAt,
                                            String reportObservation, String pendingAction,
                                            String notes, MultipartFile file) {
        WarehouseAsset a = assetRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseAsset", id));

        if (name != null) a.setName(name);
        if (categoryId != null) a.setCategoryId(categoryId);
        if (category != null) a.setCategory(category);
        if (mainCategory != null) a.setMainCategory(mainCategory);
        if (brand != null) a.setBrand(brand);
        if (model != null) a.setModel(model);
        if (serialNumber != null) a.setSerialNumber(serialNumber);
        if (color != null) a.setColor(color);
        if (status != null) a.setStatus(status);
        if (acquisitionDate != null) a.setAcquisitionDate(LocalDate.parse(acquisitionDate));
        if (initialValue != null) a.setInitialValue(new java.math.BigDecimal(initialValue));
        if (roomId != null) a.setRoomId(roomId);
        if (roomName != null) a.setRoomName(roomName);
        if (pavilionName != null) a.setPavilionName(pavilionName);
        if (sectorName != null) a.setSectorName(sectorName);
        if (teacherResponsibleId != null) a.setTeacherResponsibleId(teacherResponsibleId);
        if (teacherResponsibleName != null) a.setTeacherResponsibleName(teacherResponsibleName);
        if (warrantyUntil != null) a.setWarrantyUntil(LocalDate.parse(warrantyUntil));
        if (reportedByName != null) a.setReportedByName(reportedByName);
        if (reportedAt != null) a.setReportedAt(LocalDate.parse(reportedAt));
        if (reportObservation != null) a.setReportObservation(reportObservation);
        if (pendingAction != null) a.setPendingAction(pendingAction);
        if (notes != null) a.setNotes(notes);
        if (file != null && !file.isEmpty()) a.setPhotoUrl(storageService.upload(file, "assets"));

        return assetMap(assetRepo.save(a));
    }

    // ── 5.1 Bulk asset creation ───────────────────────────────────────────────

    @Transactional
    public Map<String, Object> bulkCreateAssets(Long institutionId,
                                                 Long categoryId, String category,
                                                 String mainCategory, String name,
                                                 Integer quantity, String brand, String model,
                                                 String color, String status,
                                                 String acquisitionDate, String initialValue,
                                                 Long roomId, String roomName, String pavilionName,
                                                 String sectorName, Long teacherResponsibleId,
                                                 String teacherResponsibleName, String warrantyUntil,
                                                 String notes, MultipartFile file) {
        if (quantity == null || quantity < 1) quantity = 1;
        if (quantity > 500) throw new BusinessException(
                "Cantidad máxima por lote es 500", HttpStatus.BAD_REQUEST, "BAD_REQUEST");

        // Upload photo once, share URL across all units
        String photoUrl = null;
        if (file != null && !file.isEmpty()) {
            photoUrl = storageService.upload(file, "assets");
        }

        java.math.BigDecimal initVal = initialValue != null ? new java.math.BigDecimal(initialValue) : null;
        LocalDate acqDate = acquisitionDate != null ? LocalDate.parse(acquisitionDate) : null;
        LocalDate warranty = warrantyUntil != null ? LocalDate.parse(warrantyUntil) : null;
        String resolvedStatus = status != null ? status : "BUENO";

        List<WarehouseAsset> created = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            WarehouseAsset asset = assetRepo.save(WarehouseAsset.builder()
                    .institutionId(institutionId)
                    .name(name)
                    .categoryId(categoryId)
                    .category(category)
                    .mainCategory(mainCategory)
                    .brand(brand)
                    .model(model)
                    .color(color)
                    .status(resolvedStatus)
                    .acquisitionDate(acqDate)
                    .initialValue(initVal)
                    .roomId(roomId)
                    .roomName(roomName)
                    .pavilionName(pavilionName)
                    .sectorName(sectorName)
                    .teacherResponsibleId(teacherResponsibleId)
                    .teacherResponsibleName(teacherResponsibleName)
                    .warrantyUntil(warranty)
                    .pendingAction("NINGUNA")
                    .photoUrl(photoUrl)
                    .notes(notes)
                    .build());

            asset.setCode(generateAssetCode(category, asset.getId()));
            created.add(assetRepo.save(asset));
        }

        List<Map<String, Object>> items = created.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("code", a.getCode());
            m.put("name", a.getName());
            m.put("categoryId", a.getCategoryId());
            m.put("category", a.getCategory());
            m.put("mainCategory", a.getMainCategory());
            m.put("status", a.getStatus());
            m.put("roomId", a.getRoomId());
            m.put("roomName", a.getRoomName());
            m.put("photoUrl", a.getPhotoUrl());
            return m;
        }).toList();

        return Map.of("items", items, "total", items.size());
    }

    // ── 6. Movements ─────────────────────────────────────────────────────────

    public List<Map<String, Object>> listMovements(Long institutionId, Long assetId) {
        List<WarehouseMovement> list = assetId != null
                ? movementRepo.findByInstitutionIdAndAssetIdOrderByOccurredAtDesc(institutionId, assetId)
                : movementRepo.findByInstitutionIdOrderByOccurredAtDesc(institutionId);
        return list.stream().map(this::movementMap).toList();
    }

    @Transactional
    public Map<String, Object> createMovement(Long institutionId, MovementRequest req) {
        WarehouseAsset asset = assetRepo.findById(req.getAssetId())
                .filter(a -> a.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseAsset", req.getAssetId()));

        WarehouseMovement m = movementRepo.save(WarehouseMovement.builder()
                .institutionId(institutionId)
                .assetId(req.getAssetId())
                .assetCode(asset.getCode())
                .assetName(asset.getName())
                .type(req.getType())
                .occurredAt(req.getOccurredAt() != null ? req.getOccurredAt() : LocalDateTime.now())
                .originRoomId(req.getOriginRoomId())
                .originLabel(req.getOriginLabel())
                .destinationRoomId(req.getDestinationRoomId())
                .destinationLabel(req.getDestinationLabel())
                .responsibleUserId(req.getResponsibleUserId())
                .responsibleUserName(req.getResponsibleUserName())
                .signedByUserId(req.getSignedByUserId())
                .signedByUserName(req.getSignedByUserName())
                .signatureUrl(req.getSignatureUrl())
                .supportNumber(req.getSupportNumber())
                .notes(req.getNotes())
                .build());

        if ("TRASLADO".equals(req.getType()) && req.getDestinationRoomId() != null) {
            asset.setRoomId(req.getDestinationRoomId());
            asset.setRoomName(req.getDestinationLabel());
            assetRepo.save(asset);
        }

        return movementMap(m);
    }

    // ── 7. Maintenance ────────────────────────────────────────────────────────

    public List<Map<String, Object>> listMaintenance(Long institutionId, Long assetId) {
        List<WarehouseMaintenance> list = assetId != null
                ? maintenanceRepo.findByInstitutionIdAndAssetIdOrderByScheduledAtDesc(institutionId, assetId)
                : maintenanceRepo.findByInstitutionIdOrderByScheduledAtDesc(institutionId);
        return list.stream().map(this::maintenanceMap).toList();
    }

    @Transactional
    public Map<String, Object> createMaintenance(Long institutionId, MaintenanceRequest req) {
        WarehouseMaintenance m = maintenanceRepo.save(WarehouseMaintenance.builder()
                .institutionId(institutionId)
                .assetId(req.getAssetId())
                .assetCode(req.getAssetCode())
                .assetName(req.getAssetName())
                .type(req.getType())
                .status(req.getStatus() != null ? req.getStatus() : "PENDIENTE")
                .scheduledAt(req.getScheduledAt())
                .startedAt(req.getStartedAt())
                .finishedAt(req.getFinishedAt())
                .providerName(req.getProviderName())
                .technicianName(req.getTechnicianName())
                .cost(req.getCost())
                .warrantyUntil(req.getWarrantyUntil())
                .responsibleUserId(req.getResponsibleUserId())
                .responsibleUserName(req.getResponsibleUserName())
                .signedByUserId(req.getSignedByUserId())
                .signedByUserName(req.getSignedByUserName())
                .signatureUrl(req.getSignatureUrl())
                .notes(req.getNotes())
                .build());
        return maintenanceMap(m);
    }

    @Transactional
    public Map<String, Object> updateMaintenance(Long id, Long institutionId, MaintenanceRequest req) {
        WarehouseMaintenance m = maintenanceRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseMaintenance", id));
        if (req.getStatus() != null) m.setStatus(req.getStatus());
        if (req.getScheduledAt() != null) m.setScheduledAt(req.getScheduledAt());
        if (req.getStartedAt() != null) m.setStartedAt(req.getStartedAt());
        if (req.getFinishedAt() != null) m.setFinishedAt(req.getFinishedAt());
        if (req.getProviderName() != null) m.setProviderName(req.getProviderName());
        if (req.getTechnicianName() != null) m.setTechnicianName(req.getTechnicianName());
        if (req.getCost() != null) m.setCost(req.getCost());
        if (req.getWarrantyUntil() != null) m.setWarrantyUntil(req.getWarrantyUntil());
        if (req.getResponsibleUserId() != null) m.setResponsibleUserId(req.getResponsibleUserId());
        if (req.getResponsibleUserName() != null) m.setResponsibleUserName(req.getResponsibleUserName());
        if (req.getSignedByUserId() != null) m.setSignedByUserId(req.getSignedByUserId());
        if (req.getSignedByUserName() != null) m.setSignedByUserName(req.getSignedByUserName());
        if (req.getSignatureUrl() != null) m.setSignatureUrl(req.getSignatureUrl());
        if (req.getNotes() != null) m.setNotes(req.getNotes());
        return maintenanceMap(maintenanceRepo.save(m));
    }

    // ── 8. Loans ──────────────────────────────────────────────────────────────

    public List<Map<String, Object>> listLoans(Long institutionId, Long assetId) {
        List<WarehouseLoan> list = assetId != null
                ? loanRepo.findByInstitutionIdAndAssetIdOrderByOutAtDesc(institutionId, assetId)
                : loanRepo.findByInstitutionIdOrderByOutAtDesc(institutionId);
        LocalDateTime now = LocalDateTime.now();
        return list.stream().map(l -> loanMap(l, now)).toList();
    }

    @Transactional
    public Map<String, Object> createLoan(Long institutionId, LoanRequest req) {
        WarehouseAsset asset = assetRepo.findById(req.getAssetId())
                .filter(a -> a.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseAsset", req.getAssetId()));

        WarehouseLoan l = loanRepo.save(WarehouseLoan.builder()
                .institutionId(institutionId)
                .assetId(req.getAssetId())
                .assetCode(asset.getCode())
                .assetName(asset.getName())
                .requesterRole(req.getRequesterRole())
                .requesterId(req.getRequesterId())
                .requesterName(req.getRequesterName())
                .outAt(req.getOutAt() != null ? req.getOutAt() : LocalDateTime.now())
                .dueAt(req.getDueAt())
                .deliveryCondition(req.getDeliveryCondition())
                .responsibleUserId(req.getResponsibleUserId())
                .responsibleUserName(req.getResponsibleUserName())
                .signedByUserId(req.getSignedByUserId())
                .signedByUserName(req.getSignedByUserName())
                .signatureUrl(req.getSignatureUrl())
                .penaltyNotes(req.getNotes())
                .status("ENTREGADO")
                .build());
        return loanMap(l, LocalDateTime.now());
    }

    @Transactional
    public Map<String, Object> updateLoan(Long id, Long institutionId, LoanRequest req) {
        WarehouseLoan l = loanRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseLoan", id));
        if (req.getDueAt() != null) l.setDueAt(req.getDueAt());
        if (req.getDeliveryCondition() != null) l.setDeliveryCondition(req.getDeliveryCondition());
        if (req.getResponsibleUserId() != null) l.setResponsibleUserId(req.getResponsibleUserId());
        if (req.getResponsibleUserName() != null) l.setResponsibleUserName(req.getResponsibleUserName());
        if (req.getSignedByUserId() != null) l.setSignedByUserId(req.getSignedByUserId());
        if (req.getSignedByUserName() != null) l.setSignedByUserName(req.getSignedByUserName());
        if (req.getSignatureUrl() != null) l.setSignatureUrl(req.getSignatureUrl());
        if (req.getNotes() != null) l.setPenaltyNotes(req.getNotes());
        return loanMap(loanRepo.save(l), LocalDateTime.now());
    }

    @Transactional
    public Map<String, Object> returnLoan(Long id, Long institutionId, LoanReturnRequest req) {
        WarehouseLoan l = loanRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseLoan", id));
        l.setReturnedAt(req.getReturnedAt() != null ? req.getReturnedAt() : LocalDateTime.now());
        l.setReturnCondition(req.getReturnCondition());
        l.setPenaltyNotes(req.getPenaltyNotes());
        l.setStatus("DEVUELTO");
        return loanMap(loanRepo.save(l), LocalDateTime.now());
    }

    // ── 9. Suppliers ─────────────────────────────────────────────────────────

    public List<Map<String, Object>> listSuppliers(Long institutionId) {
        return supplierRepo.findByInstitutionIdOrderByNameAsc(institutionId)
                .stream().map(this::supplierMap).toList();
    }

    @Transactional
    public Map<String, Object> createSupplier(Long institutionId, SupplierRequest req) {
        return supplierMap(supplierRepo.save(WarehouseSupplier.builder()
                .institutionId(institutionId)
                .name(req.getName())
                .ruc(req.getRuc())
                .contactName(req.getContactName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .notes(req.getNotes())
                .build()));
    }

    @Transactional
    public Map<String, Object> updateSupplier(Long id, Long institutionId, SupplierRequest req) {
        WarehouseSupplier s = supplierRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseSupplier", id));
        s.setName(req.getName());
        s.setRuc(req.getRuc());
        s.setContactName(req.getContactName());
        s.setPhone(req.getPhone());
        s.setEmail(req.getEmail());
        s.setAddress(req.getAddress());
        s.setNotes(req.getNotes());
        return supplierMap(supplierRepo.save(s));
    }

    // ── 10. Purchase orders ───────────────────────────────────────────────────

    public List<Map<String, Object>> listPurchaseOrders(Long institutionId) {
        return purchaseOrderRepo.findByInstitutionIdOrderByCreatedAtDesc(institutionId)
                .stream().map(this::purchaseOrderMap).toList();
    }

    @Transactional
    public Map<String, Object> createPurchaseOrder(Long institutionId, PurchaseOrderRequest req) {
        long seq = purchaseOrderRepo.countByInstitutionId(institutionId) + 1;
        String orderNumber = req.getOrderNumber() != null ? req.getOrderNumber()
                : String.format("OC-%d-%04d", LocalDate.now().getYear(), seq);

        return purchaseOrderMap(purchaseOrderRepo.save(WarehousePurchaseOrder.builder()
                .institutionId(institutionId)
                .orderNumber(orderNumber)
                .title(req.getTitle())
                .itemDescription(req.getItemDescription())
                .categoryId(req.getCategoryId())
                .categoryName(req.getCategoryName())
                .quantity(req.getQuantity())
                .urgency(req.getUrgency())
                .requestedByUserId(req.getRequestedByUserId())
                .requestedByUserName(req.getRequestedByUserName())
                .requestedAt(req.getRequestedAt() != null ? req.getRequestedAt() : LocalDate.now())
                .status(req.getStatus() != null ? req.getStatus() : "BORRADOR")
                .totalAmount(req.getTotalAmount())
                .notes(req.getNotes())
                .build()));
    }

    @Transactional
    public Map<String, Object> updatePurchaseOrder(Long id, Long institutionId,
                                                    PurchaseOrderRequest req, boolean isAdmin) {
        WarehousePurchaseOrder po = purchaseOrderRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehousePurchaseOrder", id));

        if ("APROBADO".equals(req.getStatus()) && !isAdmin) {
            throw new BusinessException("Solo ADMIN o DIRECTOR puede aprobar órdenes de compra",
                    HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        if (req.getStatus() != null) po.setStatus(req.getStatus());
        if (req.getTitle() != null) po.setTitle(req.getTitle());
        if (req.getItemDescription() != null) po.setItemDescription(req.getItemDescription());
        if (req.getCategoryId() != null) po.setCategoryId(req.getCategoryId());
        if (req.getCategoryName() != null) po.setCategoryName(req.getCategoryName());
        if (req.getQuantity() != null) po.setQuantity(req.getQuantity());
        if (req.getUrgency() != null) po.setUrgency(req.getUrgency());
        if (req.getSupplierName() != null) po.setSupplierName(req.getSupplierName());
        if (req.getSupplierId() != null) po.setSupplierId(req.getSupplierId());
        if (req.getApprovalNote() != null) po.setApprovalNote(req.getApprovalNote());
        if (req.getDocumentUrl() != null) po.setDocumentUrl(req.getDocumentUrl());
        if (req.getTotalAmount() != null) po.setTotalAmount(req.getTotalAmount());
        if (req.getNotes() != null) po.setNotes(req.getNotes());
        return purchaseOrderMap(purchaseOrderRepo.save(po));
    }

    // ── 10.2 Purchase Quotations ──────────────────────────────────────────────

    public List<Map<String, Object>> listPurchaseQuotations(Long purchaseOrderId, Long institutionId) {
        List<WarehousePurchaseQuotation> list = purchaseOrderId != null
                ? quotationRepo.findByInstitutionIdAndPurchaseOrderIdOrderByCreatedAtDesc(institutionId, purchaseOrderId)
                : quotationRepo.findByInstitutionIdOrderByCreatedAtDesc(institutionId);
        return list.stream().map(this::quotationMap).toList();
    }

    @Transactional
    public Map<String, Object> createPurchaseQuotation(Long institutionId,
                                                        PurchaseQuotationRequest req,
                                                        MultipartFile file) {
        String pdfUrl = null;
        String pdfName = null;
        if (file != null && !file.isEmpty()) {
            pdfUrl = storageService.upload(file, "warehouse/quotes");
            pdfName = file.getOriginalFilename();
        }

        WarehousePurchaseQuotation q = quotationRepo.save(WarehousePurchaseQuotation.builder()
                .institutionId(institutionId)
                .purchaseOrderId(req.getPurchaseOrderId())
                .supplierId(req.getSupplierId())
                .supplierName(req.getSupplierName())
                .quotedAt(req.getQuotedAt() != null ? req.getQuotedAt() : LocalDate.now())
                .amount(req.getAmount())
                .pdfUrl(pdfUrl)
                .pdfName(pdfName)
                .notes(req.getNotes())
                .build());
        return quotationMap(q);
    }

    @Transactional
    public Map<String, Object> updatePurchaseQuotation(Long id, Long institutionId,
                                                        PurchaseQuotationRequest req) {
        WarehousePurchaseQuotation q = quotationRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehousePurchaseQuotation", id));
        if (req.getSupplierName() != null) q.setSupplierName(req.getSupplierName());
        if (req.getAmount() != null) q.setAmount(req.getAmount());
        if (req.getNotes() != null) q.setNotes(req.getNotes());
        return quotationMap(quotationRepo.save(q));
    }

    /**
     * Approves one quotation → SELECCIONADA, others → DESCARTADA,
     * updates purchase order to APROBADO with supplier/amount.
     */
    @Transactional
    public Map<String, Object> approveQuotation(Long quotationId, Long institutionId) {
        WarehousePurchaseQuotation selected = quotationRepo.findByIdAndInstitutionId(quotationId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehousePurchaseQuotation", quotationId));

        Long poId = selected.getPurchaseOrderId();

        // Discard all others for this order
        quotationRepo.findByInstitutionIdAndPurchaseOrderId(institutionId, poId).forEach(q -> {
            if (!q.getId().equals(quotationId)) {
                q.setStatus("DESCARTADA");
                quotationRepo.save(q);
            }
        });

        selected.setStatus("SELECCIONADA");
        quotationRepo.save(selected);

        // Update purchase order
        purchaseOrderRepo.findByIdAndInstitutionId(poId, institutionId).ifPresent(po -> {
            po.setStatus("APROBADO");
            po.setApprovedQuotationId(quotationId);
            po.setSupplierId(selected.getSupplierId());
            po.setSupplierName(selected.getSupplierName());
            po.setTotalAmount(selected.getAmount());
            purchaseOrderRepo.save(po);
        });

        return quotationMap(selected);
    }

    // ── 10.3 Purchase Incidents ───────────────────────────────────────────────

    public List<Map<String, Object>> listPurchaseIncidents(Long purchaseOrderId, Long institutionId) {
        List<WarehousePurchaseIncident> list = purchaseOrderId != null
                ? incidentRepo.findByInstitutionIdAndPurchaseOrderIdOrderByReportedAtDesc(institutionId, purchaseOrderId)
                : incidentRepo.findByInstitutionIdOrderByReportedAtDesc(institutionId);
        return list.stream().map(this::incidentMap).toList();
    }

    @Transactional
    public Map<String, Object> createPurchaseIncident(Long institutionId, PurchaseIncidentRequest req) {
        WarehousePurchaseIncident inc = incidentRepo.save(WarehousePurchaseIncident.builder()
                .institutionId(institutionId)
                .purchaseOrderId(req.getPurchaseOrderId())
                .reportedAt(req.getReportedAt() != null ? req.getReportedAt() : LocalDate.now())
                .title(req.getTitle())
                .detail(req.getDetail())
                .status("ABIERTA")
                .build());

        // Auto-update order status to CON_INCIDENCIA
        purchaseOrderRepo.findByIdAndInstitutionId(req.getPurchaseOrderId(), institutionId).ifPresent(po -> {
            po.setStatus("CON_INCIDENCIA");
            purchaseOrderRepo.save(po);
        });

        return incidentMap(inc);
    }

    @Transactional
    public Map<String, Object> updatePurchaseIncident(Long id, Long institutionId,
                                                       PurchaseIncidentRequest req) {
        WarehousePurchaseIncident inc = incidentRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehousePurchaseIncident", id));
        if (req.getStatus() != null) inc.setStatus(req.getStatus());
        if (req.getTitle() != null) inc.setTitle(req.getTitle());
        if (req.getDetail() != null) inc.setDetail(req.getDetail());
        return incidentMap(incidentRepo.save(inc));
    }

    // ── 11. Inventories ───────────────────────────────────────────────────────

    public List<Map<String, Object>> listInventories(Long institutionId) {
        return inventoryRepo.findByInstitutionIdOrderByCountedAtDesc(institutionId)
                .stream().map(this::inventoryMap).toList();
    }

    @Transactional
    public Map<String, Object> createInventory(Long institutionId, InventoryRequest req) {
        int diff = (req.getPhysicalCount() != null && req.getExpectedCount() != null)
                ? req.getPhysicalCount() - req.getExpectedCount() : 0;
        return inventoryMap(inventoryRepo.save(WarehouseInventory.builder()
                .institutionId(institutionId)
                .roomId(req.getRoomId())
                .roomName(req.getRoomName())
                .countedAt(req.getCountedAt() != null ? req.getCountedAt() : LocalDateTime.now())
                .expectedCount(req.getExpectedCount())
                .physicalCount(req.getPhysicalCount())
                .difference(diff)
                .responsibleUserId(req.getResponsibleUserId())
                .responsibleUserName(req.getResponsibleUserName())
                .signedByUserId(req.getSignedByUserId())
                .signedByUserName(req.getSignedByUserName())
                .signatureUrl(req.getSignatureUrl())
                .notes(req.getNotes())
                .build()));
    }

    @Transactional
    public Map<String, Object> updateInventory(Long id, Long institutionId, InventoryRequest req) {
        WarehouseInventory inv = inventoryRepo.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("WarehouseInventory", id));
        if (req.getPhysicalCount() != null) inv.setPhysicalCount(req.getPhysicalCount());
        if (req.getExpectedCount() != null) inv.setExpectedCount(req.getExpectedCount());
        if (req.getPhysicalCount() != null && req.getExpectedCount() != null)
            inv.setDifference(req.getPhysicalCount() - req.getExpectedCount());
        if (req.getResponsibleUserId() != null) inv.setResponsibleUserId(req.getResponsibleUserId());
        if (req.getResponsibleUserName() != null) inv.setResponsibleUserName(req.getResponsibleUserName());
        if (req.getSignedByUserId() != null) inv.setSignedByUserId(req.getSignedByUserId());
        if (req.getSignedByUserName() != null) inv.setSignedByUserName(req.getSignedByUserName());
        if (req.getSignatureUrl() != null) inv.setSignatureUrl(req.getSignatureUrl());
        if (req.getNotes() != null) inv.setNotes(req.getNotes());
        return inventoryMap(inventoryRepo.save(inv));
    }

    // ── 12. Signatures ────────────────────────────────────────────────────────

    public Map<String, Object> getSignature(Long userId, Long institutionId) {
        return signatureRepo.findByUserIdAndInstitutionId(userId, institutionId)
                .map(this::signatureMap)
                .orElse(Map.of("userId", userId, "signatureUrl", "", "signerName", ""));
    }

    @Transactional
    public Map<String, Object> saveSignature(Long userId, Long institutionId,
                                              String signerName, MultipartFile file) {
        WarehouseSignature sig = signatureRepo.findByUserIdAndInstitutionId(userId, institutionId)
                .orElseGet(() -> WarehouseSignature.builder()
                        .userId(userId)
                        .institutionId(institutionId)
                        .build());
        if (signerName != null) sig.setSignerName(signerName);
        if (file != null && !file.isEmpty()) {
            sig.setSignatureUrl(storageService.upload(file, "signatures"));
        }
        return signatureMap(signatureRepo.save(sig));
    }

    // ── 13. Alerts ────────────────────────────────────────────────────────────

    public Map<String, Object> getAlerts(Long institutionId) {
        List<Map<String, Object>> items = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // Overdue loans
        loanRepo.findOverdue(institutionId, now).forEach(l -> {
            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("id", "loan-" + l.getId());
            alert.put("severity", "CRITICAL");
            alert.put("type", "PRESTAMO");
            alert.put("title", "Préstamo retrasado: " + l.getAssetName());
            alert.put("message", "Debió devolverse " + l.getDueAt());
            alert.put("relatedId", l.getId().toString());
            alert.put("dueAt", l.getDueAt());
            items.add(alert);
        });

        // Pending maintenance
        maintenanceRepo.findByInstitutionIdAndStatus(institutionId, "PENDIENTE").forEach(m -> {
            Map<String, Object> alert = new LinkedHashMap<>();
            alert.put("id", "maint-" + m.getId());
            alert.put("severity", "WARNING");
            alert.put("type", "MANTENIMIENTO");
            alert.put("title", "Mantenimiento pendiente: " + m.getAssetName());
            alert.put("message", m.getType() + " programado " + m.getScheduledAt());
            alert.put("relatedId", m.getId().toString());
            alert.put("dueAt", m.getScheduledAt());
            items.add(alert);
        });

        // Single load for all asset checks
        List<WarehouseAsset> allAssets = assetRepo.search(institutionId, null, null, null, null);
        LocalDate warrantyCutoff = today.plusDays(30);

        allAssets.stream()
                .filter(a -> a.getWarrantyUntil() != null
                        && !a.getWarrantyUntil().isBefore(today)
                        && a.getWarrantyUntil().isBefore(warrantyCutoff))
                .forEach(a -> {
                    Map<String, Object> alert = new LinkedHashMap<>();
                    alert.put("id", "warranty-" + a.getId());
                    alert.put("severity", "INFO");
                    alert.put("type", "GARANTIA");
                    alert.put("title", "Garantía por vencer: " + a.getName());
                    alert.put("message", "Vence el " + a.getWarrantyUntil());
                    alert.put("relatedId", a.getId().toString());
                    alert.put("dueAt", a.getWarrantyUntil());
                    items.add(alert);
                });

        allAssets.stream()
                .filter(a -> "REGULAR".equals(a.getStatus()) || "BAJA".equals(a.getStatus()))
                .forEach(a -> {
                    Map<String, Object> alert = new LinkedHashMap<>();
                    alert.put("id", "asset-" + a.getId());
                    alert.put("severity", "BAJA".equals(a.getStatus()) ? "CRITICAL" : "WARNING");
                    alert.put("type", "BIEN");
                    alert.put("title", "Bien en estado " + a.getStatus() + ": " + a.getName());
                    alert.put("message", "Código " + a.getCode() + " — revisar condición");
                    alert.put("relatedId", a.getId().toString());
                    alert.put("dueAt", null);
                    items.add(alert);
                });

        // Purchase orders with incident
        incidentRepo.findByInstitutionIdOrderByReportedAtDesc(institutionId).stream()
                .filter(i -> "ABIERTA".equals(i.getStatus()))
                .forEach(i -> {
                    Map<String, Object> alert = new LinkedHashMap<>();
                    alert.put("id", "incident-" + i.getId());
                    alert.put("severity", "WARNING");
                    alert.put("type", "INCIDENCIA");
                    alert.put("title", "Incidencia abierta: " + i.getTitle());
                    alert.put("message", "Orden #" + i.getPurchaseOrderId() + " — " + i.getDetail());
                    alert.put("relatedId", i.getPurchaseOrderId().toString());
                    alert.put("dueAt", null);
                    items.add(alert);
                });

        return Map.of("items", items);
    }

    // ── 14. Reports ───────────────────────────────────────────────────────────

    public Map<String, Object> getReportSummary(Long institutionId) {
        long totalAssets = assetRepo.countByInstitutionId(institutionId);
        long lowStateAssets = assetRepo.countByInstitutionIdAndStatusIn(institutionId, List.of("REGULAR", "BAJA"));
        long overdueLoans = loanRepo.findOverdue(institutionId, LocalDateTime.now()).size();
        long pendingMaintenance = maintenanceRepo.countByInstitutionIdAndStatus(institutionId, "PENDIENTE");

        List<Map<String, Object>> byCategory = assetRepo.countByCategory(institutionId).stream()
                .map(row -> Map.<String, Object>of("label", row[0], "count", row[1]))
                .toList();

        List<Map<String, Object>> byRoom = assetRepo.countByRoom(institutionId).stream()
                .map(row -> Map.<String, Object>of("label", row[0], "count", row[1]))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalAssets", totalAssets);
        result.put("lowStateAssets", lowStateAssets);
        result.put("overdueLoans", overdueLoans);
        result.put("pendingMaintenance", pendingMaintenance);
        result.put("byCategory", byCategory);
        result.put("byRoom", byRoom);
        return result;
    }

    // ── 14. Export ────────────────────────────────────────────────────────────

    public record ExportResult(byte[] bytes, String contentType, String filename) {}

    // ALMACEN-restricted datasets (cannot do full export)
    private static final Set<String> ALMACEN_ALLOWED = Set.of(
            "ACTIVOS", "ESPACIOS", "MOVIMIENTOS", "PRESTAMOS",
            "MANTENIMIENTO", "INVENTARIO", "CATEGORIAS");

    public ExportResult exportReport(Long institutionId, ExportRequest req, boolean isAdmin) {
        String dataset = req.getDataset() != null ? req.getDataset().toUpperCase() : "ACTIVOS";
        String format  = req.getFormat()  != null ? req.getFormat().toUpperCase()  : "CSV";

        if (!isAdmin && !ALMACEN_ALLOWED.contains(dataset)) {
            throw new BusinessException("Sin permiso para exportar este dataset",
                    HttpStatus.FORBIDDEN, "FORBIDDEN");
        }

        String institutionName = institutionConfigRepo.findByInstitutionId(institutionId)
                .map(InstitutionConfig::getName).orElse("Institución");
        String title = req.getTitle() != null ? req.getTitle() : dataset;

        List<Map<String, Object>> rows = fetchDataset(dataset, institutionId, req);
        List<String> fields = req.getFields() != null && !req.getFields().isEmpty()
                ? req.getFields() : defaultFields(dataset);

        try {
            return switch (format) {
                case "XLSX" -> buildXlsx(rows, fields, title, institutionName);
                case "PDF"  -> buildPdf(rows, fields, title, institutionName);
                default     -> buildCsv(rows, fields, title);
            };
        } catch (Exception e) {
            throw new BusinessException("Error generando reporte: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, "EXPORT_ERROR");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchDataset(String dataset, Long institutionId, ExportRequest req) {
        Map<String, Object> f = req.getFilters() != null ? req.getFilters() : Map.of();

        Long roomId      = toLong(f.get("roomId"));
        Long categoryId  = toLong(f.get("categoryId"));
        Long assetId     = toLong(f.get("assetId"));
        Long supplierId  = toLong(f.get("supplierId"));
        String status    = (String) f.get("status");
        String type      = (String) f.get("type");

        return switch (dataset) {
            case "ACTIVOS" -> {
                String mainCat = (String) f.get("mainCategory");
                String search  = (String) f.get("search");
                // status might be a list
                String statusFilter = status;
                if (f.get("status") instanceof List<?> list && !list.isEmpty())
                    statusFilter = list.get(0).toString();
                yield assetRepo.search(institutionId, roomId, categoryId, statusFilter, search)
                        .stream().filter(a -> mainCat == null || mainCat.equals(a.getMainCategory()))
                        .map(this::assetMap).toList();
            }
            case "CATEGORIAS" -> listAssetCategories(institutionId);
            case "ESPACIOS"   -> listRooms(institutionId, null, null);
            case "MOVIMIENTOS" -> {
                List<WarehouseMovement> list = assetId != null
                        ? movementRepo.findByInstitutionIdAndAssetIdOrderByOccurredAtDesc(institutionId, assetId)
                        : movementRepo.findByInstitutionIdOrderByOccurredAtDesc(institutionId);
                yield list.stream()
                        .filter(m -> type == null || type.equals(m.getType()))
                        .map(this::movementMap).toList();
            }
            case "PRESTAMOS" -> {
                List<WarehouseLoan> list = loanRepo.findByInstitutionIdOrderByOutAtDesc(institutionId);
                LocalDateTime now = LocalDateTime.now();
                yield list.stream()
                        .filter(l -> status == null || status.equals(l.getStatus()))
                        .map(l -> loanMap(l, now)).toList();
            }
            case "MANTENIMIENTO" -> {
                List<WarehouseMaintenance> list = maintenanceRepo.findByInstitutionIdOrderByScheduledAtDesc(institutionId);
                yield list.stream()
                        .filter(m -> status == null || status.equals(m.getStatus()))
                        .filter(m -> type == null || type.equals(m.getType()))
                        .map(this::maintenanceMap).toList();
            }
            case "INVENTARIO" -> inventoryRepo.findByInstitutionIdOrderByCountedAtDesc(institutionId)
                    .stream().map(this::inventoryMap).toList();
            case "PROVEEDORES" -> listSuppliers(institutionId);
            case "ORDENES_COMPRA" -> {
                List<WarehousePurchaseOrder> list = purchaseOrderRepo.findByInstitutionIdOrderByCreatedAtDesc(institutionId);
                yield list.stream()
                        .filter(po -> status == null || status.equals(po.getStatus()))
                        .filter(po -> supplierId == null || supplierId.equals(po.getSupplierId()))
                        .map(this::purchaseOrderMap).toList();
            }
            case "COTIZACIONES" -> listPurchaseQuotations(null, institutionId);
            case "INCIDENCIAS"  -> listPurchaseIncidents(null, institutionId);
            default -> List.of();
        };
    }

    private ExportResult buildCsv(List<Map<String, Object>> rows, List<String> fields, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", fields)).append("\n");
        for (Map<String, Object> row : rows) {
            sb.append(fields.stream()
                    .map(f -> escapeCsv(String.valueOf(row.getOrDefault(f, ""))))
                    .collect(Collectors.joining(","))).append("\n");
        }
        return new ExportResult(sb.toString().getBytes(StandardCharsets.UTF_8),
                "text/csv; charset=UTF-8", sanitize(title) + ".csv");
    }

    private ExportResult buildXlsx(List<Map<String, Object>> rows, List<String> fields,
                                    String title, String institutionName) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(title.length() > 31 ? title.substring(0, 31) : title);

            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // institution header
            Row instRow = sheet.createRow(0);
            instRow.createCell(0).setCellValue(institutionName + " — " + title);

            // header row
            Row hRow = sheet.createRow(1);
            for (int i = 0; i < fields.size(); i++) {
                Cell c = hRow.createCell(i);
                c.setCellValue(fields.get(i));
                c.setCellStyle(headerStyle);
            }

            // data rows
            for (int r = 0; r < rows.size(); r++) {
                Row dRow = sheet.createRow(r + 2);
                for (int c = 0; c < fields.size(); c++) {
                    Object val = rows.get(r).getOrDefault(fields.get(c), "");
                    dRow.createCell(c).setCellValue(val != null ? val.toString() : "");
                }
            }
            for (int i = 0; i < fields.size(); i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return new ExportResult(out.toByteArray(),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    sanitize(title) + ".xlsx");
        }
    }

    private ExportResult buildPdf(List<Map<String, Object>> rows, List<String> fields,
                                   String title, String institutionName) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Header — use fully-qualified to avoid clash with poi Font
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        com.lowagie.text.Font subFont   = FontFactory.getFont(FontFactory.HELVETICA, 10);
        com.lowagie.text.Font cellFont  = FontFactory.getFont(FontFactory.HELVETICA, 8);
        com.lowagie.text.Font hdrFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

        doc.add(new Paragraph(institutionName, titleFont));
        doc.add(new Paragraph(title + "   —   Generado: " + LocalDate.now(), subFont));
        doc.add(Chunk.NEWLINE);

        // Table
        PdfPTable table = new PdfPTable(fields.size());
        table.setWidthPercentage(100);

        // Header cells
        for (String f : fields) {
            com.lowagie.text.Font whiteFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            whiteFont.setColor(java.awt.Color.WHITE);
            PdfPCell cell = new PdfPCell(new Phrase(f, whiteFont));
            cell.setBackgroundColor(new java.awt.Color(52, 73, 94));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            table.addCell(cell);
        }

        // Data cells
        boolean alt = false;
        java.awt.Color altColor = new java.awt.Color(245, 245, 245);
        for (Map<String, Object> row : rows) {
            for (String f : fields) {
                Object val = row.getOrDefault(f, "");
                PdfPCell cell = new PdfPCell(new Phrase(val != null ? val.toString() : "", cellFont));
                cell.setPadding(3);
                if (alt) cell.setBackgroundColor(altColor);
                table.addCell(cell);
            }
            alt = !alt;
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Total registros: " + rows.size(), subFont));
        doc.close();

        return new ExportResult(out.toByteArray(), "application/pdf", sanitize(title) + ".pdf");
    }

    private List<String> defaultFields(String dataset) {
        return switch (dataset) {
            case "ACTIVOS" -> List.of("code","name","category","mainCategory","status","roomName","pavilionName","sectorName","pendingAction");
            case "CATEGORIAS" -> List.of("mainCategory","name","description","active","createdAt");
            case "ESPACIOS" -> List.of("sectorName","pavilionName","name","code","roomType","floor","capacity","aforo");
            case "MOVIMIENTOS" -> List.of("assetCode","assetName","type","occurredAt","originLabel","destinationLabel","responsibleUserName","supportNumber");
            case "PRESTAMOS" -> List.of("assetCode","assetName","requesterRole","requesterName","outAt","dueAt","returnedAt","status");
            case "MANTENIMIENTO" -> List.of("assetCode","assetName","type","status","scheduledAt","providerName","cost","responsibleUserName");
            case "INVENTARIO" -> List.of("roomName","countedAt","expectedCount","physicalCount","difference","responsibleUserName");
            case "PROVEEDORES" -> List.of("name","ruc","contactName","phone","email","address");
            case "ORDENES_COMPRA" -> List.of("orderNumber","title","categoryName","quantity","urgency","status","supplierName","totalAmount","requestedAt");
            case "COTIZACIONES" -> List.of("purchaseOrderId","supplierName","quotedAt","amount","status","notes");
            case "INCIDENCIAS" -> List.of("purchaseOrderId","reportedAt","title","detail","status");
            default -> List.of("id");
        };
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n"))
            return "\"" + val.replace("\"", "\"\"") + "\"";
        return val;
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        try { return Long.valueOf(val.toString()); } catch (Exception e) { return null; }
    }

    // ── Asset relocations ─────────────────────────────────────────────────────

    public List<Map<String, Object>> getRelocations(Long assetId, Long institutionId) {
        return movementRepo.findByInstitutionIdAndAssetIdOrderByOccurredAtDesc(institutionId, assetId)
                .stream()
                .filter(m -> "TRASLADO".equals(m.getType()))
                .map(this::movementMap)
                .toList();
    }

    // ── helpers: entity → map ─────────────────────────────────────────────────

    private Map<String, Object> sectorMap(WarehouseSector s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("institutionId", s.getInstitutionId());
        m.put("name", s.getName()); m.put("code", s.getCode());
        m.put("campusName", s.getCampusName()); m.put("notes", s.getNotes());
        m.put("createdAt", s.getCreatedAt());
        return m;
    }

    private Map<String, Object> pavilionMap(WarehousePavilion p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId()); m.put("institutionId", p.getInstitutionId());
        m.put("sectorId", p.getSectorId()); m.put("sectorName", p.getSectorName());
        m.put("name", p.getName()); m.put("code", p.getCode());
        m.put("floorCount", p.getFloorCount()); m.put("notes", p.getNotes());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    private Map<String, Object> roomMap(WarehouseRoom r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId()); m.put("institutionId", r.getInstitutionId());
        m.put("sectorId", r.getSectorId()); m.put("pavilionId", r.getPavilionId());
        m.put("sectorName", r.getSectorName()); m.put("pavilionName", r.getPavilionName());
        m.put("name", r.getName()); m.put("code", r.getCode());
        m.put("roomType", r.getRoomType()); m.put("floor", r.getFloor());
        m.put("capacity", r.getCapacity()); m.put("aforo", r.getAforo());
        m.put("teacherResponsibleId", r.getTeacherResponsibleId());
        m.put("teacherResponsibleName", r.getTeacherResponsibleName());
        m.put("notes", r.getNotes()); m.put("createdAt", r.getCreatedAt());
        return m;
    }

    private Map<String, Object> categoryMap(WarehouseAssetCategory c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId()); m.put("institutionId", c.getInstitutionId());
        m.put("mainCategory", c.getMainCategory()); m.put("name", c.getName());
        m.put("description", c.getDescription()); m.put("active", c.isActive());
        m.put("createdAt", c.getCreatedAt());
        return m;
    }

    private Map<String, Object> assetMap(WarehouseAsset a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId()); m.put("institutionId", a.getInstitutionId());
        m.put("code", a.getCode()); m.put("name", a.getName());
        m.put("categoryId", a.getCategoryId()); m.put("category", a.getCategory());
        m.put("mainCategory", a.getMainCategory());
        m.put("brand", a.getBrand()); m.put("model", a.getModel());
        m.put("serialNumber", a.getSerialNumber()); m.put("color", a.getColor());
        m.put("status", a.getStatus());
        m.put("acquisitionDate", a.getAcquisitionDate()); m.put("initialValue", a.getInitialValue());
        m.put("photoUrl", a.getPhotoUrl()); m.put("roomId", a.getRoomId());
        m.put("roomName", a.getRoomName()); m.put("pavilionName", a.getPavilionName());
        m.put("sectorName", a.getSectorName());
        m.put("teacherResponsibleId", a.getTeacherResponsibleId());
        m.put("teacherResponsibleName", a.getTeacherResponsibleName());
        m.put("warrantyUntil", a.getWarrantyUntil());
        m.put("reportedByName", a.getReportedByName());
        m.put("reportedAt", a.getReportedAt());
        m.put("reportObservation", a.getReportObservation());
        m.put("pendingAction", a.getPendingAction());
        m.put("notes", a.getNotes()); m.put("createdAt", a.getCreatedAt());
        return m;
    }

    private Map<String, Object> movementMap(WarehouseMovement mv) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", mv.getId()); m.put("institutionId", mv.getInstitutionId());
        m.put("assetId", mv.getAssetId()); m.put("assetCode", mv.getAssetCode());
        m.put("assetName", mv.getAssetName()); m.put("type", mv.getType());
        m.put("occurredAt", mv.getOccurredAt()); m.put("originRoomId", mv.getOriginRoomId());
        m.put("originLabel", mv.getOriginLabel()); m.put("destinationRoomId", mv.getDestinationRoomId());
        m.put("destinationLabel", mv.getDestinationLabel());
        m.put("responsibleUserId", mv.getResponsibleUserId());
        m.put("responsibleUserName", mv.getResponsibleUserName());
        m.put("signedByUserId", mv.getSignedByUserId());
        m.put("signedByUserName", mv.getSignedByUserName());
        m.put("signatureUrl", mv.getSignatureUrl());
        m.put("supportNumber", mv.getSupportNumber()); m.put("notes", mv.getNotes());
        m.put("createdAt", mv.getCreatedAt());
        return m;
    }

    private Map<String, Object> maintenanceMap(WarehouseMaintenance mnt) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", mnt.getId()); m.put("institutionId", mnt.getInstitutionId());
        m.put("assetId", mnt.getAssetId()); m.put("assetCode", mnt.getAssetCode());
        m.put("assetName", mnt.getAssetName()); m.put("type", mnt.getType());
        m.put("status", mnt.getStatus()); m.put("scheduledAt", mnt.getScheduledAt());
        m.put("startedAt", mnt.getStartedAt()); m.put("finishedAt", mnt.getFinishedAt());
        m.put("providerName", mnt.getProviderName()); m.put("technicianName", mnt.getTechnicianName());
        m.put("cost", mnt.getCost()); m.put("warrantyUntil", mnt.getWarrantyUntil());
        m.put("responsibleUserId", mnt.getResponsibleUserId());
        m.put("responsibleUserName", mnt.getResponsibleUserName());
        m.put("signedByUserId", mnt.getSignedByUserId());
        m.put("signedByUserName", mnt.getSignedByUserName());
        m.put("signatureUrl", mnt.getSignatureUrl());
        m.put("notes", mnt.getNotes()); m.put("createdAt", mnt.getCreatedAt());
        return m;
    }

    private Map<String, Object> loanMap(WarehouseLoan l, LocalDateTime now) {
        String effectiveStatus = l.getStatus();
        if ("ENTREGADO".equals(effectiveStatus) && l.getDueAt() != null && l.getDueAt().isBefore(now)) {
            effectiveStatus = "RETRASADO";
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId()); m.put("institutionId", l.getInstitutionId());
        m.put("assetId", l.getAssetId()); m.put("assetCode", l.getAssetCode());
        m.put("assetName", l.getAssetName()); m.put("requesterRole", l.getRequesterRole());
        m.put("requesterId", l.getRequesterId()); m.put("requesterName", l.getRequesterName());
        m.put("outAt", l.getOutAt()); m.put("dueAt", l.getDueAt());
        m.put("returnedAt", l.getReturnedAt()); m.put("deliveryCondition", l.getDeliveryCondition());
        m.put("returnCondition", l.getReturnCondition()); m.put("penaltyNotes", l.getPenaltyNotes());
        m.put("responsibleUserId", l.getResponsibleUserId());
        m.put("responsibleUserName", l.getResponsibleUserName());
        m.put("signedByUserId", l.getSignedByUserId());
        m.put("signedByUserName", l.getSignedByUserName());
        m.put("signatureUrl", l.getSignatureUrl());
        m.put("status", effectiveStatus); m.put("createdAt", l.getCreatedAt());
        return m;
    }

    private Map<String, Object> supplierMap(WarehouseSupplier s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("institutionId", s.getInstitutionId());
        m.put("name", s.getName()); m.put("ruc", s.getRuc());
        m.put("contactName", s.getContactName()); m.put("phone", s.getPhone());
        m.put("email", s.getEmail()); m.put("address", s.getAddress());
        m.put("notes", s.getNotes());
        return m;
    }

    private Map<String, Object> purchaseOrderMap(WarehousePurchaseOrder po) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", po.getId()); m.put("institutionId", po.getInstitutionId());
        m.put("orderNumber", po.getOrderNumber()); m.put("title", po.getTitle());
        m.put("itemDescription", po.getItemDescription());
        m.put("categoryId", po.getCategoryId()); m.put("categoryName", po.getCategoryName());
        m.put("quantity", po.getQuantity()); m.put("urgency", po.getUrgency());
        m.put("requestedByUserId", po.getRequestedByUserId());
        m.put("requestedByUserName", po.getRequestedByUserName());
        m.put("requestedAt", po.getRequestedAt()); m.put("status", po.getStatus());
        m.put("approvedQuotationId", po.getApprovedQuotationId());
        m.put("supplierId", po.getSupplierId()); m.put("supplierName", po.getSupplierName());
        m.put("approvalNote", po.getApprovalNote()); m.put("documentUrl", po.getDocumentUrl());
        m.put("totalAmount", po.getTotalAmount()); m.put("notes", po.getNotes());
        m.put("createdAt", po.getCreatedAt());
        return m;
    }

    private Map<String, Object> quotationMap(WarehousePurchaseQuotation q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", q.getId()); m.put("institutionId", q.getInstitutionId());
        m.put("purchaseOrderId", q.getPurchaseOrderId());
        m.put("supplierId", q.getSupplierId()); m.put("supplierName", q.getSupplierName());
        m.put("quotedAt", q.getQuotedAt()); m.put("amount", q.getAmount());
        m.put("pdfUrl", q.getPdfUrl()); m.put("pdfName", q.getPdfName());
        m.put("notes", q.getNotes()); m.put("status", q.getStatus());
        m.put("createdAt", q.getCreatedAt());
        return m;
    }

    private Map<String, Object> incidentMap(WarehousePurchaseIncident i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId()); m.put("institutionId", i.getInstitutionId());
        m.put("purchaseOrderId", i.getPurchaseOrderId());
        m.put("reportedAt", i.getReportedAt()); m.put("title", i.getTitle());
        m.put("detail", i.getDetail()); m.put("status", i.getStatus());
        m.put("createdAt", i.getCreatedAt());
        return m;
    }

    private Map<String, Object> inventoryMap(WarehouseInventory inv) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", inv.getId()); m.put("institutionId", inv.getInstitutionId());
        m.put("roomId", inv.getRoomId()); m.put("roomName", inv.getRoomName());
        m.put("countedAt", inv.getCountedAt()); m.put("expectedCount", inv.getExpectedCount());
        m.put("physicalCount", inv.getPhysicalCount()); m.put("difference", inv.getDifference());
        m.put("responsibleUserId", inv.getResponsibleUserId());
        m.put("responsibleUserName", inv.getResponsibleUserName());
        m.put("signedByUserId", inv.getSignedByUserId());
        m.put("signedByUserName", inv.getSignedByUserName());
        m.put("signatureUrl", inv.getSignatureUrl());
        m.put("notes", inv.getNotes());
        return m;
    }

    private Map<String, Object> signatureMap(WarehouseSignature s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", s.getUserId()); m.put("signerName", s.getSignerName());
        m.put("signatureUrl", s.getSignatureUrl()); m.put("updatedAt", s.getUpdatedAt());
        return m;
    }

    // ── code generator ────────────────────────────────────────────────────────

    private String generateAssetCode(String category, Long id) {
        String prefix = category != null && category.length() >= 3
                ? category.substring(0, 3).toUpperCase()
                : "ACT";
        return prefix + "-" + String.format("%06d", id);
    }
}
