package org.pgno20.medimart.config;

import org.pgno20.medimart.model.User;
import org.pgno20.medimart.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class SuperAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final org.pgno20.medimart.repository.SupplierRepository supplierRepository;
    private final org.pgno20.medimart.repository.MedicineOrderRepository orderRepository;

    public SuperAdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate,
                                 org.pgno20.medimart.repository.SupplierRepository supplierRepository,
                                 org.pgno20.medimart.repository.MedicineOrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.supplierRepository = supplierRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Safely set defaults for both columns in users table to prevent MySQL default value errors
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN active TINYINT(1) DEFAULT 1");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN is_active TINYINT(1) DEFAULT 1");
        } catch (Exception ignored) {}

        String superAdminEmail = "superadmin@medimart.com";

        if (!userRepository.existsByEmail(superAdminEmail)) {
            User superAdmin = new User();
            superAdmin.setFullName("Super Administrator");
            superAdmin.setEmail(superAdminEmail);
            superAdmin.setPassword(passwordEncoder.encode("SuperAdmin@123"));
            superAdmin.setDob(LocalDate.of(2000, 1, 1));
            superAdmin.setGender("Other");
            superAdmin.setRoleName("ROLE_ADMIN");
            superAdmin.setActive(true);

            userRepository.save(superAdmin);
            System.out.println("[SuperAdminInitializer] Default super admin created: " + superAdminEmail);
        } else {
            User superAdmin = userRepository.findByEmail(superAdminEmail).get();
            superAdmin.setPassword(passwordEncoder.encode("SuperAdmin@123"));
            userRepository.save(superAdmin);
            System.out.println("[SuperAdminInitializer] Super admin already exists. Password reset to BCrypt.");
        }

        // Seed Suppliers
        if (supplierRepository.count() == 0) {
            System.out.println("[SuperAdminInitializer] Seeding default suppliers...");
            org.pgno20.medimart.entity.Supplier s1 = new org.pgno20.medimart.entity.Supplier();
            s1.setId("SUP001");
            s1.setName("PharmaSys Lanka Ltd");
            s1.setType(org.pgno20.medimart.entity.SupplierType.WHOLESALER.name());
            s1.setContact("+94 11 234 5678");
            s1.setEmail("contact@pharmasys.lk");
            s1.setAddress("123 Pharma Road, Colombo 03");
            s1.setMedicinesSupplied("Penadol, Amoxicillin, Vitamin C");
            supplierRepository.save(s1);

            org.pgno20.medimart.entity.Supplier s2 = new org.pgno20.medimart.entity.Supplier();
            s2.setId("SUP002");
            s2.setName("Global Corporation Medicine");
            s2.setType(org.pgno20.medimart.entity.SupplierType.MANUFACTURER.name());
            s2.setContact("+94 77 123 4567");
            s2.setEmail("info@globalmed.lk");
            s2.setAddress("45 Industry Park, Biyagama");
            s2.setMedicinesSupplied("Ibuprofen, Metformin, Cetirizine");
            supplierRepository.save(s2);

            // Create a supplier user for SUP001
            if (!userRepository.existsByEmail("contact@pharmasys.lk")) {
                User sUser = new User();
                sUser.setFullName("PharmaSys Admin");
                sUser.setEmail("contact@pharmasys.lk");
                sUser.setPassword(passwordEncoder.encode("Supplier@123"));
                sUser.setRoleName("ROLE_SUPPLIER");
                sUser.setActive(true);
                userRepository.save(sUser);
            }
        }

        // Seed Orders
        if (orderRepository.count() == 0 && supplierRepository.count() > 0) {
            System.out.println("[SuperAdminInitializer] Seeding default supplier orders...");
            
            org.pgno20.medimart.entity.MedicineOrder o1 = new org.pgno20.medimart.entity.MedicineOrder();
            o1.setSupplierId("SUP001");
            o1.setMedicineName("Vitamin C");
            o1.setQuantity(500);
            o1.setUnitPrice(15.50);
            o1.setExpectedDelivery(LocalDate.now().plusDays(3));
            o1.setDeliveryMode("STANDARD");
            o1.setStatus("PENDING");
            o1.calculateAndSetTotals(org.pgno20.medimart.entity.SupplierType.WHOLESALER);
            orderRepository.save(o1);

            org.pgno20.medimart.entity.MedicineOrder o2 = new org.pgno20.medimart.entity.MedicineOrder();
            o2.setSupplierId("SUP001");
            o2.setMedicineName("Penadol");
            o2.setQuantity(1000);
            o2.setUnitPrice(9.82);
            o2.setExpectedDelivery(LocalDate.now().minusDays(1));
            o2.setDeliveryMode("EXPRESS");
            o2.setStatus("DELIVERED");
            o2.calculateAndSetTotals(org.pgno20.medimart.entity.SupplierType.WHOLESALER);
            orderRepository.save(o2);
            
            org.pgno20.medimart.entity.MedicineOrder o3 = new org.pgno20.medimart.entity.MedicineOrder();
            o3.setSupplierId("SUP002");
            o3.setMedicineName("Ibuprofen");
            o3.setQuantity(200);
            o3.setUnitPrice(25.00);
            o3.setExpectedDelivery(LocalDate.now().plusDays(5));
            o3.setDeliveryMode("STANDARD");
            o3.setStatus("REJECTED");
            o3.calculateAndSetTotals(org.pgno20.medimart.entity.SupplierType.MANUFACTURER);
            orderRepository.save(o3);
        }
        
        // Retroactively ensure all suppliers have a login account
        System.out.println("[SuperAdminInitializer] Syncing Supplier User accounts...");
        for (org.pgno20.medimart.entity.Supplier s : supplierRepository.findAll()) {
            if (s.getEmail() != null && !s.getEmail().isBlank()) {
                User sUser = userRepository.findByEmail(s.getEmail()).orElse(new User());
                if (sUser.getId() == null) {
                    sUser.setFullName(s.getName() + " Admin");
                    sUser.setEmail(s.getEmail());
                    sUser.setGender("Other");
                    sUser.setDob(LocalDate.now());
                }
                // Force update role and password so they can definitely log in for the demo
                sUser.setPassword(passwordEncoder.encode("Supplier@123")); 
                sUser.setRoleName("ROLE_SUPPLIER");
                sUser.setActive(true);
                userRepository.save(sUser);
                System.out.println("Created/Updated login for supplier: " + s.getEmail());
            }
        }
    }
}
