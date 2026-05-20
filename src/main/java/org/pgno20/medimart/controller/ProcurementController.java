package org.pgno20.medimart.controller;

import org.pgno20.medimart.entity.*;
import org.pgno20.medimart.service.ProcurementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement")
public class
ProcurementController {

    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    public static class CreateRequestDto {
        public Long medicineId;
        public String medicineName;
        public Integer quantity;
        public Double targetPrice;
    }

    @PostMapping("/requests")
    public ResponseEntity<ProcurementRequest> createRequest(@RequestBody CreateRequestDto dto) {
        return ResponseEntity.ok(procurementService.createRequest(dto.medicineId, dto.medicineName, dto.quantity, dto.targetPrice));
    }

    @PutMapping("/requests/{id}")
    public ResponseEntity<ProcurementRequest> updateRequest(@PathVariable Long id, @RequestBody CreateRequestDto dto) {
        try {
            return ResponseEntity.ok(procurementService.updateRequest(id, dto.quantity, dto.targetPrice));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ProcurementRequest>> getAllRequests() {
        return ResponseEntity.ok(procurementService.getAllRequests());
    }

    @GetMapping("/requests/open")
    public ResponseEntity<List<ProcurementRequest>> getOpenRequests() {
        return ResponseEntity.ok(procurementService.getOpenRequests());
    }

    public static class SubmitBidDto {
        public Long requestId;
        public Double price;
    }

    @PostMapping("/bids")
    public ResponseEntity<?> submitBid(@RequestBody SubmitBidDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).build();
        }
        String supplierEmail = auth.getName();
        try {
            return ResponseEntity.ok(procurementService.submitBid(dto.requestId, supplierEmail, dto.price));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/requests/{requestId}/bids")
    public ResponseEntity<List<SupplierBid>> getBidsForRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(procurementService.getBidsForRequest(requestId));
    }

    @PostMapping("/bids/{bidId}/accept")
    public ResponseEntity<?> acceptBid(@PathVariable Long bidId) {
        try {
            return ResponseEntity.ok(procurementService.acceptBid(bidId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bids/{bidId}/reject")
    public ResponseEntity<?> rejectBid(@PathVariable Long bidId) {
        try {
            procurementService.rejectBid(bidId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
