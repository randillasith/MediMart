package org.pgno20.medimart.controller;

import jakarta.validation.Valid;
import org.pgno20.medimart.dto.StockBatchRequest;
import org.pgno20.medimart.dto.StockBatchResponse;
import org.pgno20.medimart.service.StockBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines/{medicineId}/batches")
public class StockBatchController {

    private final StockBatchService stockBatchService;

    public StockBatchController(StockBatchService stockBatchService) {
        this.stockBatchService = stockBatchService;
    }

    // List all batches for a medicine
    @GetMapping
    public ResponseEntity<List<StockBatchResponse>> listBatches(@PathVariable Long medicineId) {
        return ResponseEntity.ok(stockBatchService.getBatchesForMedicine(medicineId));
    }

    // Add a new batch (restock)
    @PostMapping
    public ResponseEntity<StockBatchResponse> addBatch(
            @PathVariable Long medicineId,
            @Valid @RequestBody StockBatchRequest req) {
        return ResponseEntity.ok(stockBatchService.addBatch(medicineId, req));
    }

    // Update an existing batch
    @PutMapping("/{batchId}")
    public ResponseEntity<StockBatchResponse> updateBatch(
            @PathVariable Long medicineId,
            @PathVariable Long batchId,
            @Valid @RequestBody StockBatchRequest req) {
        return ResponseEntity.ok(stockBatchService.updateBatch(medicineId, batchId, req));
    }

    // Delete a batch
    @DeleteMapping("/{batchId}")
    public ResponseEntity<Void> deleteBatch(
            @PathVariable Long medicineId,
            @PathVariable Long batchId) {
        stockBatchService.deleteBatch(medicineId, batchId);
        return ResponseEntity.noContent().build();
    }
}
