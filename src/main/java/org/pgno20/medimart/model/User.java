package org.pgno20.medimart.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false) // Keep this, but we will handle the "blank" update in the Controller
    private String password; 

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false)
    private String gender;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /** Default shipping address saved from the checkout form. */
    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;
}