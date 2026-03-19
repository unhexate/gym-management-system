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

Design pattern classes live inside `service/` — as natural parts of the architecture, not in a separate showcase folder.

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

## Running Tests

```bash
mvn test
```

| Test class | Type | Tests |
|------------|------|-------|
| `UserFactoryTest` | Unit | 6 |
| `PricingStrategyTest` | Unit | 3 |
| `BaseCrudServiceTest` | Unit | 3 |
| `UserServiceTest` | Service (Mockito) | 5 |
| `MembershipServiceTest` | Service (Mockito) | 7 |
| `GymApiIntegrationTest` | Integration (MockMvc) | 15 |
| **Total** | | **39** |

---

## Project Structure (Actual)

```
gym-management-system/
├── src/main/java/com/gym/
│   ├── GymApplication.java
│   ├── controller/          # 6 REST controllers
│   ├── dto/                 # Request DTOs with Bean Validation
│   ├── exception/           # ApiResponse, custom exceptions, GlobalExceptionHandler
│   ├── model/               # 11 JPA entities
│   ├── repository/          # 9 Spring Data JPA interfaces
│   └── service/             # Business logic + all design pattern classes
│       ├── UserFactory.java          # Factory Pattern
│       ├── BaseCrudService.java      # Template Method Pattern (abstract base)
│       ├── PricingStrategy.java      # Strategy Pattern (interface)
│       ├── BasicPricing.java         # Strategy – no discount
│       ├── PremiumPricing.java       # Strategy – 20% discount
│       └── GymManagementFacade.java  # Facade Pattern
```

---

## Progress

- [x] Project scaffold (pom.xml, application.properties, GymApplication.java)
- [x] 11 JPA entity classes (User hierarchy + domain models)
- [x] 9 Spring Data JPA repository interfaces
- [x] Request/response DTOs with Bean Validation
- [x] Exception handling (ApiResponse, GlobalExceptionHandler, custom exceptions)
- [x] Factory Pattern – UserFactory + UserService (POST /api/users)
- [x] Template Method Pattern – BaseCrudService (base for Membership/Payment/Workout/Attendance services)
- [x] Strategy Pattern – PricingStrategy + BasicPricing + PremiumPricing (POST /api/memberships)
- [x] Facade Pattern – GymManagementFacade + ReportService (POST /api/payments, GET /api/reports)
- [x] 6 REST controllers covering all 10 API endpoints
- [x] Unit tests: UserFactory (6), PricingStrategy (3), BaseCrudService (3)
- [x] Service tests: UserService (5), MembershipService (7) – Mockito
- [x] Integration tests: all 10 endpoints via MockMvc + H2 (15 tests)
- [x] **39 tests – 0 failures**
