<div align="center">

  <img src="https://github.com/randillasith/MediMart/blob/main/src/main/resources/images/logo.png" alt="MediMart Logo" width="120"/>

  # MediMart – Online Medical Store

  **Clinical Sophistication & Care**

  <p>
    <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=java&logoColor=white" />
    <img src="https://img.shields.io/badge/Spring_Boot-4.0-6DB33F?logo=springboot&logoColor=white" />
    <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white" />
    <img src="https://img.shields.io/badge/Thymeleaf-005F0F?logo=thymeleaf&logoColor=white" />
    <img src="https://img.shields.io/badge/Tailwind_CSS-06B6D4?logo=tailwindcss&logoColor=white" />
  </p>

  <br/>
</div>

---

**MediMart** is a full-stack pharmacy e-commerce and inventory management system developed as a **mini-project** for the **1st Year, 2nd Semester** module **Object-Oriented Programming (OOP)**, under the **Information Technology degree pathway** at **Sri Lanka Institute of Information Technology (SLIIT)**.

The project focuses on building a complete online pharmacy platform that handles medicine catalog browsing, prescription management, order processing, stock control, supplier management, and procurement with bidding — all while demonstrating core OOP principles such as **inheritance, polymorphism, encapsulation, and abstraction** throughout the domain models.

---

## Live Website

🔗 [https://medimart.randillasith.me/](https://medimart.randillasith.me/)

---

## Project Objectives

- Build a functional **online pharmacy storefront** where customers can browse and purchase medicines
- Implement a **prescription management system** for uploading, verifying, and tracking prescriptions
- Develop a **full admin dashboard** for inventory control, order processing, and system configuration
- Create a **supplier procurement system** with bidding capabilities for stock replenishment
- Track **stock batches** with expiry dates for quality and safety compliance
- Demonstrate practical use of **OOP concepts** — inheritance, polymorphism, encapsulation, and abstraction
- Provide **role-based access control** for customers, suppliers, and administrators
- Enable **auto-deployment** via CI/CD pipeline (GitHub Actions → VPS)

---

## System Overview

```mermaid
graph TB
    subgraph Frontend["FRONTEND (HTML/JS)"]
        A1["Storefront (Static HTML/JS)"]
        A2["Admin Dashboard (Static HTML/JS)"]
        A3["Thymeleaf Admin (Server-Rendered)"]
    end

    subgraph Backend["SPRING BOOT APPLICATION"]
        direction TB
        B1["REST API CONTROLLERS<br/>AuthController · MedicineController · OrderController<br/>PrescriptionController · SupplierController · CategoryController<br/>ProcurementController · StockBatchController · DashboardController<br/>UserController · SystemSettingsController"]
        B2["SERVICE LAYER<br/>Business Logic · Validation · Calculations<br/>Orchestration Across Domain Objects"]
        B3["DATA ACCESS (JPA REPOSITORIES)<br/>CRUD Operations · Custom Queries · Pagination"]
    end

    subgraph Database["DATABASE (MySQL)"]
        C1["medicines · categories · orders · order_items<br/>users · prescriptions · suppliers<br/>supplier_bids · procurement_requests<br/>stock_batches · system_settings"]
    end

    Frontend -->|"fetch() JSON / Thymeleaf"| B1
    B1 --> B2
    B2 --> B3
    B3 --> Database

    style Frontend fill:#1a1a2e,stroke:#e94560,color:#fff
    style Backend fill:#16213e,stroke:#0f3460,color:#fff
    style Database fill:#0f3460,stroke:#e94560,color:#fff
```

**How it works:**

1. **Customers** browse the medicine catalog and add items to their cart
2. **Prescriptions** can be uploaded for medicines that require doctor approval
3. **Orders** are placed with shipping details and delivery fee calculation
4. **Admins** manage inventory, process orders, and verify prescriptions via the dashboard
5. **Suppliers** participate in procurement requests by submitting bids
6. **Stock batches** are tracked individually with expiry dates and purchase prices
7. **System settings** (tax rates, fees, thresholds) are configurable at runtime

---

## System Architecture Diagram

```mermaid
graph TB
    subgraph Client["CLIENT BROWSER"]
        direction LR
        S["Storefront<br/>(HTML/JS)"]
        A["Admin Panel<br/>(Thymeleaf)"]
    end

    subgraph Spring["SPRING BOOT APPLICATION"]
        direction TB
        Sec["SECURITY FILTER CHAIN<br/>Session Auth · Role Check · CSRF"]
        Ctrl["REST CONTROLLERS<br/>JSON API Endpoints"]
        Srv["SERVICE LAYER<br/>Business Logic"]
        JPA["JPA REPOSITORY LAYER<br/>Data Access"]
        Sec --> Ctrl
        Ctrl --> Srv
        Srv --> JPA
    end

    subgraph MySQL["MySQL DATABASE"]
        DB["medicines · categories<br/>orders · order_items<br/>users · prescriptions<br/>suppliers · supplier_bids<br/>procurement_requests<br/>stock_batches · system_settings<br/>feedbacks"]
    end

    subgraph CI["GITHUB ACTIONS (CI/CD)"]
        Deploy["Push → Build → Deploy to VPS"]
    end

    Client -->|HTTP Request| Sec
    JPA -->|JPA/Hibernate| MySQL
    MySQL -.->|"on push to main"| CI

    style Client fill:#1a1a2e,stroke:#e94560,color:#fff
    style Spring fill:#16213e,stroke:#0f3460,color:#fff
    style MySQL fill:#0f3460,stroke:#e94560,color:#fff
    style CI fill:#1a1a2e,stroke:#533483,color:#fff
```

The diagram above illustrates the overall architecture of the MediMart Pharmacy Management System, showing how the frontend communicates with the Spring Boot backend via REST APIs, which processes business logic through the service layer, persists data to the MySQL database via JPA repositories, and is automatically deployed through a GitHub Actions CI/CD pipeline.

---

## OOP Concepts Demonstrated

| Concept | Implementation |
|---|---|
| **🔷 Inheritance** | `Medicine` → `OTCMedicine` — OTC extends base Medicine with specialized pricing<br/>`AbstractSupplier` → `Supplier` — shared base with polymorphic supply display |
| **🔶 Polymorphism** | `Medicine.getFinalPrice()` — OTC returns `price × 1.10`, base returns price<br/>`Supplier.getSupplierCategory()` — different lead times per type (LOCAL=3d, IMPORTED=14d, GOVERNMENT=30d) |
| **🔷 Encapsulation** | All entity fields are private with controlled public getters/setters<br/>Business logic is hidden in Service layer, data access in Repository layer |
| **🔶 Abstraction** | Controllers know *what* services do, not *how* they do it<br/>Services hide database complexity behind clean method signatures |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 4.0.5, Spring Data JPA, Spring Security, Spring Validation |
| **Frontend** | HTML5, Tailwind CSS (CDN), JavaScript (fetch API), Thymeleaf (admin templates) |
| **Database** | MySQL 8.0 |
| **Build** | Maven, Lombok |

---

## Key Features

| Module | Features |
|---|---|
| 🛒 **Storefront** | Catalog browsing, search, cart, checkout, prescription upload |
| 🔐 **Authentication** | Session-based login/register, BCrypt password hashing, role-based access |
| ⚕️ **Prescriptions** | Image upload, admin verification, PENDING→APPROVED/REJECTED workflow |
| 📊 **Admin Dashboard** | Revenue, order stats, low stock alerts, user counts, recent orders |
| 📦 **Inventory** | Medicine CRUD, SKU generation, soft delete, image upload |
| 📋 **Orders** | Full lifecycle (PENDING→PROCESSING→SHIPPED→DELIVERED/CANCELLED) |
| 📦 **Stock Batches** | Batch tracking, expiry dates, purchase price per batch |
| 🤝 **Suppliers** | CRUD with LOCAL/IMPORTED/GOVERNMENT types and lead times |
| 💰 **Procurement** | Requests with target prices, supplier bidding, bid acceptance |
| ⚙️ **Settings** | Tax rates, delivery fees, low stock threshold, maintenance mode |

---

## API Endpoints

| Method | Path | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/register` | Create account | Public |
| `POST` | `/api/auth/login` | Sign in | Public |
| `GET` | `/api/medicines/storefront` | Browse catalog | Public |
| `POST` | `/api/orders` | Place order | Public |
| `GET` | `/api/dashboard/metrics` | Admin metrics | Admin |
| `POST` | `/api/medicines` | Add medicine | Admin |
| `POST` | `/api/prescriptions` | Upload prescription | Authenticated |
| `POST` | `/api/procurement/requests` | Create procurement | Admin |
| `POST` | `/api/procurement/bids` | Submit supplier bid | Supplier |

*Full API reference with all endpoints is available in the [complete source code](https://github.com/randillasith/MediMart).*

---

## Wiki

📖 Detailed documentation is available on the [MediMart Wiki](https://github.com/randillasith/MediMart/wiki) including:

- 🚀 [Getting Started](https://github.com/randillasith/MediMart/wiki/Getting-Started) — Setup guide
- 🏗️ [Architecture](https://github.com/randillasith/MediMart/wiki/Architecture) — System design
- 📡 [API Reference](https://github.com/randillasith/MediMart/wiki/API-Reference) — All endpoints
- 🧱 [OOP Concepts](https://github.com/randillasith/MediMart/wiki/OOP-Concepts) — Code examples
- 🗄️ [Database Schema](https://github.com/randillasith/MediMart/wiki/Database-Schema) — Table structures
- 🔐 [Security](https://github.com/randillasith/MediMart/wiki/Security) — Auth flow
- ☁️ [Deployment](https://github.com/randillasith/MediMart/wiki/Deployment) — CI/CD

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.


