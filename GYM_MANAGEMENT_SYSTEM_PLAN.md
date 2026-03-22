# Gym Management System - Simplified Plan

## Overview
A simplified Java-based gym management system using Spring MVC architecture with four design patterns. Focus on core features with **10 essential API endpoints**.

> **Aligned with class diagram** (User, Admin, Receptionist, Member, Trainer, Report, WorkoutPlan, Attendance, Membership, MembershipPlan, Payment) and **use case diagram** (Register, Enroll Membership, Process Payment, Manage Workouts, Attendance, Reports, Trainer Assignment).

---

## Technology Stack

- **Java 17+**
- **Spring Boot 3.x** (includes Spring MVC, Spring Data JPA)
- **H2 Database** (for simplicity, can switch to MySQL later)
- **Maven** (Build tool)
- **Lombok** (Reduce boilerplate)

---

## Actors (from Use Case Diagram)

| Actor | Role |
|-------|------|
| **Member** | Registers, enrolls in memberships, makes payments, views workouts, marks attendance |
| **Admin** | Adds trainers, creates membership plans, assigns trainers, views reports |
| **Receptionist** | Registers members, collects offline payments |
| **Trainer** | Creates/updates workout plans, views assigned members |
| **Payment Gateway** | Processes online payments (external system) |

---

## MVC Architecture

### 1. Model Layer
- **Entities**: User (abstract), Admin, Receptionist, Member, Trainer, Report, WorkoutPlan, Attendance, Membership, MembershipPlan, Payment
- **Repositories**: Spring Data JPA repositories

### 2. View Layer
- **REST APIs**: Simple JSON responses (10 endpoints)

### 3. Controller Layer
- **Controllers**: UserController, MembershipController, PaymentController, WorkoutController, AttendanceController, ReportController
- **Exception Handler**: Basic error handling

### 4. Service Layer
- **Business Logic**: Implement 4 design patterns here
- **Services**: UserService, MembershipService, PaymentService, WorkoutService, AttendanceService, ReportService

---

## Design Patterns (4 Required)

### 1. **Factory Pattern** (Creational)
- **Category**: Creational Pattern
- **Where**: `UserFactory` class
- **Why**: Create Admin, Receptionist, Member, or Trainer objects based on role without exposing instantiation logic
- **Implementation**: `createUser(String role)` returns appropriate User subclass (Admin/Receptionist/Member/Trainer)
- **Benefit**: Encapsulates object creation, easy to extend with new user types

### 2. **Facade Pattern** (Structural)
- **Category**: Structural Pattern
- **Where**: `GymManagementFacade` class
- **Why**: Provide simplified interface to complex subsystems (membership enrollment, payment, workout, attendance)
- **Implementation**: Single entry point that coordinates MembershipService, PaymentService, WorkoutService, and AttendanceService
- **Benefit**: Simplifies client interaction, reduces coupling between subsystems

### 3. **Strategy Pattern** (Behavioral)
- **Category**: Behavioral Pattern
- **Where**: `PricingStrategy` interface
- **Why**: Different pricing algorithms for membership plans (Basic/Premium)
- **Implementation**: 
  - Interface: `PricingStrategy`
  - Implementations: `BasicPricing`, `PremiumPricing`
  - Context: `MembershipService` uses appropriate strategy at runtime based on `MembershipPlan`
- **Benefit**: Open/Closed principle - can add new plan pricing without modifying existing code

### 4. **Template Method Pattern** (Behavioral)
- **Category**: Behavioral Pattern
- **Where**: `BaseCrudService` abstract class
- **Why**: Define skeleton of CRUD operations, let subclasses override specific steps
- **Implementation**:
  - Abstract class: `BaseCrudService` with template methods
  - Concrete classes: `MembershipService`, `PaymentService`, `WorkoutService` extend base
  - Common steps (validation, save, logging) in template, specific logic overridden
- **Benefit**: Reusability, consistency across CRUD operations, reduces code duplication

### 5. **MVC Pattern** (Framework-Enforced)
- **Category**: Architectural Pattern (enforced by Spring Framework)
- **Where**: Entire application structure
- **Why**: Separation of concerns between data, business logic, and presentation
- **Implementation**: Spring MVC with Controllers, Services (Model logic), and REST responses (View)
- **Benefit**: Clear separation of responsibilities, easier testing and maintenance

---

## Design Principles (4 Required)

### 1. **Single Responsibility Principle (SRP)**
- **What**: Each class should have only one reason to change
- **Where Applied**: 
  - Controllers handle only HTTP requests/responses
  - Services contain only business logic
  - Repositories handle only data access
  - Each entity (Member, Trainer, Payment, etc.) represents a single domain concept
- **Benefit**: Easier to maintain, test, and understand

### 2. **Dependency Inversion Principle (DIP)**
- **What**: Depend on abstractions, not concrete implementations
- **Where Applied**:
  - Services depend on Repository interfaces (Spring Data JPA)
  - `MembershipService` depends on `PricingStrategy` interface, not `BasicPricing`/`PremiumPricing` directly
  - `GymManagementFacade` depends on service interfaces, not concrete service classes
- **Benefit**: Loose coupling, easier to swap implementations, better testability

### 3. **Open/Closed Principle (OCP)**
- **What**: Open for extension, closed for modification
- **Where Applied**:
  - New pricing strategies can be added without modifying `MembershipService`
  - New user roles can be added by extending `User` and updating `UserFactory`
  - New report types can be added by extending `ReportService`
- **Benefit**: System can grow without breaking existing functionality

### 4. **Separation of Concerns (SoC)**
- **What**: Different concerns should be handled by different modules
- **Where Applied**:
  - MVC architecture separates presentation, business logic, and data
  - `pattern/` package isolates design pattern implementations from business code
  - Exception handling separated in `GlobalExceptionHandler`
  - Payment processing isolated from membership logic via `PaymentService`
- **Benefit**: Modular code, easier debugging, better organization

---

## Simplified Project Structure

```
gym-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/gym/
│   │   │   ├── GymApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java
│   │   │   │   ├── MembershipController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── WorkoutController.java
│   │   │   │   ├── AttendanceController.java
│   │   │   │   └── ReportController.java
│   │   │   ├── model/
│   │   │   │   ├── User.java (abstract)
│   │   │   │   ├── Admin.java
│   │   │   │   ├── Receptionist.java
│   │   │   │   ├── Member.java
│   │   │   │   ├── Trainer.java
│   │   │   │   ├── MembershipPlan.java
│   │   │   │   ├── Membership.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── WorkoutPlan.java
│   │   │   │   ├── Attendance.java
│   │   │   │   └── Report.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── MemberRepository.java
│   │   │   │   ├── TrainerRepository.java
│   │   │   │   ├── MembershipRepository.java
│   │   │   │   ├── MembershipPlanRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   ├── WorkoutPlanRepository.java
│   │   │   │   ├── AttendanceRepository.java
│   │   │   │   └── ReportRepository.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── MembershipService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── WorkoutService.java
│   │   │   │   ├── AttendanceService.java
│   │   │   │   └── ReportService.java
│   │   │   ├── pattern/
│   │   │   │   ├── factory/
│   │   │   │   │   └── UserFactory.java
│   │   │   │   ├── facade/
│   │   │   │   │   └── GymManagementFacade.java
│   │   │   │   ├── strategy/
│   │   │   │   │   ├── PricingStrategy.java
│   │   │   │   │   ├── BasicPricing.java
│   │   │   │   │   └── PremiumPricing.java
│   │   │   │   └── template/
│   │   │   │       └── BaseCrudService.java
│   │   │   └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

---

## 10 Core API Endpoints

> Endpoints are derived from use cases in the use case diagram.

### User Management (2 endpoints)

**1. POST /api/users**
- Register a new user (Member, Trainer, Admin, or Receptionist)
- Uses **Factory Pattern** to instantiate the correct User subclass
- Request: `{ name, email, phone, password, role (MEMBER/TRAINER/ADMIN/RECEPTIONIST) }`
- Response: Created user details

**2. PUT /api/users/{userId}/profile**
- Update user profile (any role)
- Request: `{ name, phone, password }`
- Response: Updated user details

---

### Membership Management (2 endpoints)

**3. POST /api/memberships**
- Enroll member in a membership plan
- Uses **Strategy Pattern** for pricing (BasicPricing / PremiumPricing based on plan)
- Uses **Template Method** for standard create flow
- Request: `{ memberId, planId }`
- Response: Membership details with calculated price and dates

**4. GET /api/memberships/member/{memberId}**
- View active membership status for a member
- Response: `{ membershipId, planName, startDate, endDate, status }`

---

### Payment (2 endpoints)

**5. POST /api/payments**
- Process an online payment (integrates with Payment Gateway)
- Uses **Facade Pattern** to coordinate MembershipService + PaymentService
- Uses **Template Method** for standard create flow
- Request: `{ memberId, membershipId, amount, paymentMode }`
- Response: Payment confirmation with invoice details

**6. GET /api/payments/member/{memberId}**
- View payment history for a member
- Response: List of past payments with amounts and dates

---

### Workout Plan (2 endpoints)

**7. POST /api/workouts**
- Trainer creates a workout plan for an assigned member
- Request: `{ trainerId, memberId, exercises, schedule, difficultyLevel }`
- Response: Created workout plan

**8. GET /api/workouts/member/{memberId}**
- Member views their workout schedule
- Response: Workout plan with exercises and schedule

---

### Attendance & Reports (2 endpoints)

**9. POST /api/attendance**
- Member marks attendance for a session
- Uses **Template Method** for standard create flow
- Request: `{ memberId, checkinTime }`
- Response: Attendance record

**10. GET /api/reports**
- Admin views system-wide reports (member stats, revenue, attendance)
- Uses **Facade Pattern** to aggregate data from multiple services
- Response: `{ totalMembers, activeMembers, totalRevenue, attendanceSummary }`

---

## Design Pattern Usage in Endpoints

| Endpoint | Design Pattern | Usage |
|----------|----------------|-------|
| POST /api/users | **Factory** (Creational) | Creates Admin / Receptionist / Member / Trainer object |
| POST /api/payments, GET /api/reports | **Facade** (Structural) | Coordinates multiple services in a single call |
| POST /api/memberships | **Strategy** (Behavioral) | Calculates price based on membership plan type |
| POST /api/memberships, POST /api/payments, POST /api/attendance | **Template Method** (Behavioral) | Standardized create/read/update/delete flow |
| All endpoints | **MVC** (Framework) | Spring MVC architecture throughout |

---

## Simplified Database Schema

> Schema mirrors the class diagram entities.

**users** (base table)
- id, name, email, phone, password, role (MEMBER/TRAINER/ADMIN/RECEPTIONIST)

**members** (joined to users)
- id (FK → users), join_date, status

**trainers** (joined to users)
- id (FK → users), specialization, experience_years

**membership_plans**
- id, plan_name, duration (months), price, description

**memberships**
- id, member_id (FK → members), plan_id (FK → membership_plans), start_date, end_date, status

**payments**
- id, member_id (FK → members), membership_id (FK → memberships), amount, date, payment_mode, payment_status

**workout_plans**
- id, trainer_id (FK → trainers), member_id (FK → members), exercises, schedule, difficulty_level

**attendance**
- id, member_id (FK → members), date, checkin_time

**reports**
- id, report_type, generated_date, generated_by (FK → users)

---

## Simple Response Format

```json
{
  "success": true,
  "data": { ... },
  "message": "Optional message"
}
```

---

## Implementation Steps

### Step 1: Setup (Day 1)
1. Create Spring Boot project with Maven
2. Setup H2 database in application.properties
3. Create entity classes (11 entities from class diagram)
4. Create repositories (9 repositories)

### Step 2: Design Patterns (Day 2)
1. Implement Factory - `UserFactory` (Creational) — supports all 4 roles
2. Implement Facade - `GymManagementFacade` (Structural) — enrollment + payment + reports
3. Implement Strategy - `PricingStrategy` + implementations (Behavioral)
4. Implement Template Method - `BaseCrudService` (Behavioral)

### Step 3: Services & Controllers (Day 3-4)
1. Create 6 services (User, Membership, Payment, Workout, Attendance, Report)
2. Create 6 controllers
3. Implement 10 API endpoints covering all use cases
4. Add basic exception handling

### Step 4: Testing (Day 5)
1. Test all 10 endpoints with sample data for each actor (Member, Admin, Receptionist, Trainer)
2. Verify design patterns work correctly
3. Create README with API documentation

---

## Design Pattern Implementation Guide

### 1. Factory Pattern (Creational)
**Purpose**: Create Admin, Receptionist, Member, or Trainer without exposing instantiation logic

```java
// Factory class
public class UserFactory {
    public static User createUser(String role, String name, String email, String phone, String password) {
        return switch (role.toUpperCase()) {
            case "MEMBER"       -> new Member(name, email, phone, password);
            case "TRAINER"      -> new Trainer(name, email, phone, password);
            case "ADMIN"        -> new Admin(name, email, phone, password);
            case "RECEPTIONIST" -> new Receptionist(name, email, phone, password);
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }
}
```

### 2. Facade Pattern (Structural)
**Purpose**: Provide simplified interface to coordinate enrollment, payment, and report subsystems

```java
@Service
public class GymManagementFacade {
    @Autowired private MembershipService membershipService;
    @Autowired private PaymentService paymentService;
    @Autowired private ReportService reportService;

    // Coordinates membership creation + payment processing in one call
    public PaymentResponse enrollAndPay(Long memberId, Long planId, String paymentMode) {
        Membership membership = membershipService.enroll(memberId, planId);
        Payment payment = paymentService.process(memberId, membership.getId(), membership.getPrice(), paymentMode);
        return new PaymentResponse(payment);
    }

    // Coordinates aggregation from multiple services for reports
    public ReportResponse generateReport() {
        return reportService.aggregate(membershipService.stats(), paymentService.revenue());
    }
}
```

### 3. Strategy Pattern (Behavioral)
**Purpose**: Different pricing algorithms based on membership plan type

```java
// Strategy interface
public interface PricingStrategy {
    double calculatePrice(double basePrice);
}

// Concrete strategies
public class BasicPricing implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice) {
        return basePrice; // No discount
    }
}

public class PremiumPricing implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * 0.8; // 20% discount
    }
}

// Usage in MembershipService
@Service
public class MembershipService extends BaseCrudService<Membership, Long> {
    public Membership enroll(Long memberId, Long planId) {
        MembershipPlan plan = membershipPlanRepository.findById(planId).orElseThrow();
        PricingStrategy strategy = plan.getPlanName().equalsIgnoreCase("PREMIUM")
            ? new PremiumPricing()
            : new BasicPricing();
        double price = strategy.calculatePrice(plan.getPrice());
        // ... create and save membership with calculated price
    }
}
```

### 4. Template Method Pattern (Behavioral)
**Purpose**: Define skeleton of CRUD operations in base class, let subclasses customize specific steps

```java
// Abstract template class
public abstract class BaseCrudService<T, ID> {

    // Template method - defines the algorithm skeleton
    public T create(T entity) {
        validate(entity);           // Step 1: Validate
        beforeSave(entity);         // Step 2: Pre-processing (hook)
        T saved = save(entity);     // Step 3: Save to DB
        afterSave(saved);           // Step 4: Post-processing (hook)
        return saved;
    }

    // Template method for delete
    public void delete(ID id) {
        T entity = findById(id);
        beforeDelete(entity);       // Hook method
        performDelete(id);
        afterDelete(entity);        // Hook method
    }

    protected abstract T save(T entity);
    protected abstract T findById(ID id);
    protected abstract void performDelete(ID id);

    // Hook methods - subclasses can override
    protected void validate(T entity) {}
    protected void beforeSave(T entity) {}
    protected void afterSave(T entity) {}
    protected void beforeDelete(T entity) {}
    protected void afterDelete(T entity) {}
}

// Concrete implementation — MembershipService
@Service
public class MembershipService extends BaseCrudService<Membership, Long> {
    @Autowired private MembershipRepository repository;

    @Override
    protected Membership save(Membership m) { return repository.save(m); }

    @Override
    protected Membership findById(Long id) { return repository.findById(id).orElseThrow(); }

    @Override
    protected void performDelete(Long id) { repository.deleteById(id); }

    @Override
    protected void validate(Membership m) {
        if (m.getMember() == null) throw new IllegalArgumentException("Member is required");
    }

    // Apply pricing strategy before saving
    @Override
    protected void beforeSave(Membership m) {
        PricingStrategy strategy = resolvePricingStrategy(m.getPlan());
        m.setPrice(strategy.calculatePrice(m.getPlan().getPrice()));
    }
}
```

**MVC Pattern**: Automatically enforced by Spring Framework through Controllers (handle HTTP), Services (business logic), and Repositories (data access).

---

## Pattern & Principle Summary

### Design Patterns Overview
| Pattern | Category | Class/Component | Purpose |
|---------|----------|-----------------|----------|
| Factory | Creational | UserFactory | Create Admin/Receptionist/Member/Trainer objects |
| Facade | Structural | GymManagementFacade | Simplify enrollment + payment + report interactions |
| Strategy | Behavioral | PricingStrategy | Dynamic pricing per membership plan |
| Template Method | Behavioral | BaseCrudService | Standardize CRUD operation flow |
| MVC | Architectural | Spring MVC | Separation of concerns (Controller/Service/Repository) |

---

## Key Entities Summary

> Matches the class diagram directly.

1. **User** (abstract) - Base class with login/logout/updateProfile
2. **Admin** - Extends User; manages trainers, plans, reports
3. **Receptionist** - Extends User; registers members, collects offline payments
4. **Member** - Extends User; enrolls memberships, attends, makes payments
5. **Trainer** - Extends User; creates/updates workout plans, views assigned members
6. **MembershipPlan** - Plan definitions (name, duration, price)
7. **Membership** - Member's active subscription to a plan
8. **Payment** - Payment record (online or offline)
9. **WorkoutPlan** - Exercise schedule assigned by trainer to member
10. **Attendance** - Daily attendance record for a member
11. **Report** - System report generated by admin

---

## Success Criteria

✅ 10 working API endpoints covering all use cases from the use case diagram  
✅ 4 design patterns clearly implemented (1 Creational, 1 Structural, 2 Behavioral)  
✅ 4 design principles applied (SRP, DIP, OCP, SoC)  
✅ MVC architecture enforced by Spring Framework  
✅ H2 database with 11 entities matching the class diagram  
✅ All actors supported: Member, Admin, Receptionist, Trainer, Payment Gateway  
✅ All use cases covered: Registration, Membership, Payment, Workout, Attendance, Reports, Trainer Assignment

---

## Conclusion

This gym management system is aligned with the class diagram and use case diagram:
- **11 entities** matching the class diagram exactly (User hierarchy + domain models)
- **5 actors** from the use case diagram each with appropriate endpoints
- **10 core API endpoints** covering all major use cases
- **4 design patterns** (Factory, Facade, Strategy, Template Method) + MVC by framework
- **4 design principles** (SRP, DIP, OCP, SoC)
- **Spring MVC** architecture

The system demonstrates:
- **1 Creational pattern** (Factory — all 4 user roles)
- **1 Structural pattern** (Facade — enrollment + payment + reports)
- **2 Behavioral patterns** (Strategy — pricing; Template Method — CRUD flow)
- **1 Framework pattern** (MVC)
- Clean SOLID and OOP principles aligned with the provided diagrams.
