# Gym Management System

A Java-based gym management system built with **Spring Boot 3.x** and **Spring MVC** architecture, demonstrating four Gang-of-Four design patterns backed by an H2 in-memory database.

---

## Technology Stack

| Technology | Version |
|------------|---------|
| Java | 17+ |
| Spring Boot | 3.2.0 |
| Spring MVC / Spring Data JPA | (included with Boot) |
| H2 Database | runtime |
| Lombok | latest |
| Maven | build tool |

---

## Architecture

The project follows **Spring MVC** with four layers:

```
Controller → Service → Repository → Database
```

Design patterns are isolated in the `pattern/` package.

---

## Design Patterns

| Pattern | Category | Class | Purpose |
|---------|----------|-------|---------|
| Factory | Creational | `UserFactory` | Create Admin / Receptionist / Member / Trainer |
| Facade | Structural | `GymManagementFacade` | Coordinate enrollment + payment + reports |
| Strategy | Behavioral | `PricingStrategy` | Dynamic pricing per membership plan type |
| Template Method | Behavioral | `BaseCrudService` | Standardise CRUD operation flow |

---

## Design Principles

- **SRP** – Controllers handle HTTP; Services hold business logic; Repositories manage data access
- **DIP** – Services depend on repository interfaces and the `PricingStrategy` abstraction
- **OCP** – New pricing strategies or user roles require no modification of existing classes
- **SoC** – MVC layers + isolated `pattern/` package + dedicated `GlobalExceptionHandler`

---

## Project Structure

```
gym-management-system/
├── src/main/java/com/gym/
│   ├── GymApplication.java
│   ├── controller/          # REST controllers (6 files)
│   ├── model/               # JPA entities (11 files)
│   ├── repository/          # Spring Data JPA repos (9 files)
│   ├── service/             # Business logic (6 files)
│   ├── pattern/
│   │   ├── factory/         # UserFactory
│   │   ├── facade/          # GymManagementFacade
│   │   ├── strategy/        # PricingStrategy, BasicPricing, PremiumPricing
│   │   └── template/        # BaseCrudService
│   └── exception/           # GlobalExceptionHandler
└── src/main/resources/
    └── application.properties
```

---

## API Endpoints

| # | Method | URL | Description | Pattern |
|---|--------|-----|-------------|---------|
| 1 | POST | `/api/users` | Register a new user | Factory |
| 2 | PUT | `/api/users/{id}/profile` | Update user profile | — |
| 3 | POST | `/api/memberships` | Enroll in a plan | Strategy + Template |
| 4 | GET | `/api/memberships/member/{id}` | View membership status | — |
| 5 | POST | `/api/payments` | Process payment | Facade + Template |
| 6 | GET | `/api/payments/member/{id}` | Payment history | — |
| 7 | POST | `/api/workouts` | Create workout plan | — |
| 8 | GET | `/api/workouts/member/{id}` | View workout schedule | — |
| 9 | POST | `/api/attendance` | Mark attendance | Template |
| 10 | GET | `/api/reports` | Admin reports | Facade |

---

## Running the Application

```bash
cd gym-management-system
mvn spring-boot:run
```

H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
JDBC URL: `jdbc:h2:mem:gymdb`

---

## Progress

- [x] Project scaffold (pom.xml, application.properties, GymApplication.java)
