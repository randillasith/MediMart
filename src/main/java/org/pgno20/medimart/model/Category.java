package org.pgno20.medimart.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=80)
    private String name;

    @Column(nullable=false, length=20)
    private String status = "ACTIVE"; // ACTIVE / INACTIVE

    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    // getters/setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
}