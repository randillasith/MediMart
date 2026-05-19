package org.pgno20.medimart.repository;

import org.pgno20.medimart.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, String> {
    List<Supplier> findByNameContainingIgnoreCase(String name);
    @Query("SELECT s FROM Supplier s WHERE LOWER(s.medicinesSupplied) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Supplier> findByMedicinesSuppliedContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT MAX(s.id) FROM Supplier s")
    String findMaxId();

    java.util.Optional<Supplier> findByEmail(String email);
}