package org.pgno20.medimart.repository;

import org.pgno20.medimart.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, String> {
    List<Supplier> findByNameContainingIgnoreCase(String name);
    List<Supplier> findByMedicinesSuppliedContainingIgnoreCase(String keyword);
}