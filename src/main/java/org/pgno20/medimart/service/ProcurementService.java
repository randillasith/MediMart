package org.pgno20.medimart.service;

import org.pgno20.medimart.entity.*;
import org.pgno20.medimart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProcurementService {
    
    private final ProcurementRequestRepository requestRepository;
    private final SupplierBidRepository bidRepository;
    private final MedicineOrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;

    public ProcurementService(ProcurementRequestRepository requestRepository, 
                              SupplierBidRepository bidRepository,
                              MedicineOrderRepository orderRepository,
                              SupplierRepository supplierRepository,
                              MedicineRepository medicineRepository) {
        this.requestRepository = requestRepository;
        this.bidRepository = bidRepository;
        this.orderRepository = orderRepository;
        this.supplierRepository = supplierRepository;
        this.medicineRepository = medicineRepository;
    }

    public ProcurementRequest createRequest(Long medicineId, String medicineName, Integer quantity, Double targetPrice) {
        ProcurementRequest req = new ProcurementRequest();
        req.setMedicineId(medicineId);
        req.setMedicineName(medicineName);
        req.setQuantityRequired(quantity);
        req.setTargetPrice(targetPrice);
        return requestRepository.save(req);
    }

    public ProcurementRequest updateRequest(Long id, Integer quantity, Double targetPrice) {
        ProcurementRequest req = requestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!"OPEN".equals(req.getStatus())) {
            throw new IllegalStateException("Cannot edit a closed request");
        }
        req.setQuantityRequired(quantity);
        req.setTargetPrice(targetPrice);
        return requestRepository.save(req);
    }

    public List<ProcurementRequest> getOpenRequests() {
        return requestRepository.findByStatusOrderByCreatedAtDesc("OPEN");
    }

    public List<ProcurementRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public SupplierBid submitBid(Long requestId, String supplierEmail, Double price) {
        ProcurementRequest req = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!"OPEN".equals(req.getStatus())) {
            throw new IllegalStateException("Request is not open for bidding");
        }

        if (req.getTargetPrice() != null && price > req.getTargetPrice()) {
            throw new IllegalArgumentException("Bid price exceeds the Admin Target Price.");
        }

        String normalizedEmail = supplierEmail != null ? supplierEmail.trim() : "";
        Supplier supplier = supplierRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> {
                Supplier newSupplier = new Supplier();
                newSupplier.setId("SUP" + (System.currentTimeMillis() % 100000));
                newSupplier.setName("Supplier " + normalizedEmail.split("@")[0]);
                newSupplier.setEmail(normalizedEmail);
                newSupplier.setType("WHOLESALER");
                newSupplier.setContact("000000000");
                return supplierRepository.save(newSupplier);
            });

        SupplierBid bid = new SupplierBid();
        bid.setProcurementRequest(req);
        bid.setSupplierId(supplier.getId());
        bid.setSupplierName(supplier.getName());
        bid.setPrice(price);
        
        return bidRepository.save(bid);
    }

    public List<SupplierBid> getBidsForRequest(Long requestId) {
        return bidRepository.findByProcurementRequestId(requestId);
    }

    @Transactional
    public MedicineOrder acceptBid(Long bidId) {
        SupplierBid bid = bidRepository.findById(bidId)
            .orElseThrow(() -> new IllegalArgumentException("Bid not found"));

        ProcurementRequest req = bid.getProcurementRequest();
        if (!"OPEN".equals(req.getStatus())) {
            throw new IllegalStateException("Request is already closed");
        }

        // Mark bid as accepted
        bid.setStatus("ACCEPTED");
        bidRepository.save(bid);

        // Mark request as closed
        req.setStatus("CLOSED");
        requestRepository.save(req);

        // Reject all other bids for this requesaaaxt
        List<SupplierBid> otherBids = bidRepository.findByProcurementRequestId(req.getId());
        for (SupplierBid other : otherBids) {
            if (!other.getId().equals(bid.getId())) {
                other.setStatus("REJECTED");
                bidRepository.save(other);
            }
        }

        // Create the MedicineOrder
        Supplier supplier = supplierRepository.findById(bid.getSupplierId())
            .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        MedicineOrder order = new MedicineOrder();
        order.setSupplierId(supplier.getId());
        order.setSupplierName(supplier.getName());
        order.setSupplierType(supplier.getType());
        order.setMedicineName(req.getMedicineName());
        order.setQuantity(req.getQuantityRequired());
        order.setUnitPrice(bid.getPrice());
        order.setOrderDate(LocalDate.now());
        order.setStatus("ACCEPTED");
        // Default calculate
        order.calculateAndSetTotals(supplier.getSupplierTypeEnum());
        
        return orderRepository.save(order);
    }

    @Transactional
    public void rejectBid(Long bidId) {
        SupplierBid bid = bidRepository.findById(bidId)
            .orElseThrow(() -> new IllegalArgumentException("Bid not found"));

        ProcurementRequest req = bid.getProcurementRequest();
        if (!"OPEN".equals(req.getStatus())) {
            throw new IllegalStateException("Request is already closed");
        }

        if ("ACCEPTED".equals(bid.getStatus())) {
            throw new IllegalStateException("Cannot reject an already accepted bid");
        }

        bid.setStatus("REJECTED");
        bidRepository.save(bid);
    }
}
