# ExpenseJar — Personal Expense Tracker with Categories

## 1. Project Objective
Students often overspend their monthly allowance because they have no simple, structured way to track where money goes across categories like food, travel, and stationery. 

**ExpenseJar** is a full-featured application comprising a robust Spring Boot RESTful backend and a modern web dashboard that enables users to:
- Log personal expenses categorized by purpose with dates and amounts.
- Set monthly spending budgets per category.
- Monitor total spending per category for the current month.
- Automatically receive alerts whenever spending reaches or exceeds **90%** of a category's monthly budget.
- View month-over-month spending trends calculated dynamically from database records.

---

## 2. Technologies Used
- **Backend Language**: Java 17
- **Framework**: Spring Boot 3.2.5
- **Modules**:
  - Spring Web (RESTful API controllers & static asset serving)
  - Spring Data JPA (Hibernate ORM persistence)
  - Spring Validation (Jakarta Bean Validation)
- **Database**: MySQL 8.0+
- **Database Driver**: MySQL Connector/J
- **Frontend Dashboard**: HTML5, Vanilla CSS3 (modern dark mode, glassmorphism, responsive grid), Vanilla JavaScript (ES6+ fetch API)
- **Build Tool**: Maven
- **Architecture**: Layered Architecture (`Controller` → `Service` → `Repository` → `Database`)

---

## 3. Project Structure
```
ExpenseJar/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── expensejar/
│   │   │               ├── ExpenseJarApplication.java
│   │   │               ├── controller/
│   │   │               │   ├── UserController.java
│   │   │               │   ├── CategoryController.java
│   │   │               │   ├── ExpenseController.java
│   │   │               │   └── BudgetController.java
│   │   │               ├── service/
│   │   │               │   ├── UserService.java
│   │   │               │   ├── CategoryService.java
│   │   │               │   ├── ExpenseService.java
│   │   │               │   └── BudgetService.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── CategoryRepository.java
│   │   │               │   ├── ExpenseRepository.java
│   │   │               │   └── BudgetRepository.java
│   │   │               ├── entity/
│   │   │               │   ├── User.java
│   │   │               │   ├── Category.java
│   │   │               │   ├── Expense.java
│   │   │               │   └── Budget.java
│   │   │               ├── dto/
│   │   │               │   ├── UserRequest.java
│   │   │               │   ├── CategoryRequest.java
│   │   │               │   ├── ExpenseRequest.java
│   │   │               │   ├── BudgetRequest.java
│   │   │               │   ├── ExpenseResponse.java
│   │   │               │   ├── BudgetAlertResponse.java
│   │   │               │   ├── CategorySummaryResponse.java
│   │   │               │   └── MonthlyTrendResponse.java
│   │   │               └── exception/
│   │   │                   ├── GlobalExceptionHandler.java
│   │   │                   ├── ResourceNotFoundException.java
│   │   │                   └── InvalidExpenseException.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/
│   │           │   └── style.css
│   │           └── js/
│   │               └── app.js
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── expensejar/
│                       ├── ExpenseServiceTest.java
│                       ├── ExpenseControllerTest.java
│                       ├── UserControllerTest.java
│                       └── BudgetControllerTest.java
```

---

## 4. MySQL Database Setup

1. Open your MySQL client (MySQL Workbench, MySQL Command Line Client, or terminal).
2. Execute the following command to create the database:
```sql
CREATE DATABASE expensejar;
```
*(Hibernate/JPA will automatically create and update all tables: `users`, `categories`, `expenses`, and `budgets` when the application starts.)*

---

## 5. MySQL Configuration Steps

Open `src/main/resources/application.properties` and replace `YOUR_MYSQL_PASSWORD` with your actual MySQL password:

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/expensejar?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate / JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Server Port
server.port=8080
```

> **Note**: If your MySQL user is not `root` or runs on a different port than `3306`, update `spring.datasource.username` and `spring.datasource.url` accordingly.

---

## 6. How to Open in IntelliJ IDEA

1. Launch **IntelliJ IDEA**.
2. Click **Open** (or **File** → **Open...**).
3. Navigate to and select the `ExpenseJar` folder (containing `pom.xml`).
4. Click **OK** / **Open as Project**.
5. IntelliJ IDEA will automatically recognize it as a Maven project and download all required dependencies.
6. Ensure the Project SDK is set to **Java 17** (**File** → **Project Structure** → **Project** → **SDK**: Java 17).

---

## 7. How to Run the Application & Access the Dashboard

### Option A: From IntelliJ IDEA
1. In the Project Explorer, navigate to:
   `src/main/java/com/example/expensejar/ExpenseJarApplication.java`
2. Right-click on `ExpenseJarApplication` and select **Run 'ExpenseJarApplication'**.
3. Open your browser and visit:
   **`http://localhost:8080/`** to view the interactive Frontend Dashboard!

### Option B: Using Maven CLI
Run the following command from the project root:
```bash
mvn spring-boot:run
```
Then visit **`http://localhost:8080/`** in any web browser.

---

## 8. Frontend Dashboard Features

The web frontend (`src/main/resources/static/index.html`) provides a modern dark-mode interface:
1. **User Switcher & Management**: Select any user from the dropdown or click `+` to add a new user.
2. **Real-time Metric Cards**: Displays Current Month Total Spend, Total Monthly Budget, Percentage of Budget Used, and Active Alerts.
3. **Action Center (Tabs)**:
   - **Log Expense**: Category selector, positive amount input with validation, and date picker. Shows immediate alert toasts.
   - **Set Budget**: Sets monthly budget for any category with year-month selection.
   - **New Category**: Quickly create custom categories on the fly.
4. **Current Month Category Breakdown**: Interactive category cards with budget comparison progress bars and safe/warning/alert status tags.
5. **Month-over-Month Spending Trend**: Dynamic bar visualization of historical spending per month.
6. **All Logged Expenses Table**: Complete history of logged expenses with category badges and instant delete options.

---

## 9. Business Rules Enforced

1. **RULE 1 — Positive Expense Amount**:
   - Every expense amount must be strictly greater than zero (`amount > 0`).
   - If an amount is `0` or negative (e.g., `-100`), the request is immediately rejected with HTTP `400 BAD_REQUEST` and a clear error message: `"Expense amount must be greater than zero"`.
   - The expense is **never** saved to the database.
   - Enforced in both **Spring Validation** (`@Positive`) and the **Service Layer** (`ExpenseService`).

2. **RULE 2 — Budget Alert Recalculation**:
   - Budget alerts are recalculated **every time** a new expense is logged.
   - The system finds the total spent for that user, category, and month.
   - Compares total spent against the set category budget:
     $$\text{percentageUsed} = \left(\frac{\text{totalSpent}}{\text{budgetAmount}}\right) \times 100$$
   - If $\text{percentageUsed} \ge 90\%$, `alert: true` and `message: "Budget alert"` is returned.
   - If $\text{percentageUsed} < 90\%$, `alert: false` and `message: "Expense added successfully"` is returned.

3. **RULE 3 — Dynamic Calculation**:
   - Spending summaries and month-over-month trends are computed directly from actual MySQL records. No hard-coded numbers are used.

---

## 10. REST API Endpoints Summary

| Module | Method | Endpoint | Description |
|---|---|---|---|
| **User** | `POST` | `/api/users` | Create a new user |
| **User** | `GET` | `/api/users` | Get all users |
| **User** | `GET` | `/api/users/{id}` | Get user by ID |
| **User** | `PUT` | `/api/users/{id}` | Update user details |
| **User** | `DELETE` | `/api/users/{id}` | Delete user by ID |
| **Category** | `POST` | `/api/categories` | Create a new category |
| **Category** | `GET` | `/api/categories` | Get all categories |
| **Category** | `GET` | `/api/categories/{id}` | Get category by ID |
| **Category** | `PUT` | `/api/categories/{id}` | Update category name |
| **Category** | `DELETE` | `/api/categories/{id}` | Delete category by ID |
| **Budget** | `POST` | `/api/budgets` | Set or update monthly category budget |
| **Budget** | `GET` | `/api/budgets` | Get all budgets |
| **Budget** | `GET` | `/api/budgets/{id}` | Get budget by ID |
| **Budget** | `GET` | `/api/budgets/user/{userId}` | Get budgets by User ID |
| **Budget** | `GET` | `/api/budgets/category/{categoryId}` | Get budgets by Category ID |
| **Budget** | `DELETE` | `/api/budgets/{id}` | Delete budget by ID |
| **Expense** | `POST` | `/api/expenses` | Add expense (returns 90% budget alert status) |
| **Expense** | `GET` | `/api/expenses` | Get all expenses |
| **Expense** | `GET` | `/api/expenses/{id}` | Get expense by ID |
| **Expense** | `GET` | `/api/expenses/user/{userId}` | Get expenses by User ID |
| **Expense** | `GET` | `/api/expenses/category/{categoryId}` | Get expenses by Category ID |
| **Expense** | `GET` | `/api/expenses/summary/current-month/{userId}` | Current month spending grouped by category |
| **Expense** | `GET` | `/api/expenses/trend/{userId}` | Month-over-month spending trend |
| **Expense** | `DELETE` | `/api/expenses/{id}` | Delete expense by ID |

---

## 11. Sample Requests & Responses

### 1. Create User
- **Endpoint**: `POST /api/users`
- **Request Body**:
```json
{
  "name": "Sakthi",
  "email": "sakthi@example.com"
}
```
- **Response** (`201 CREATED`):
```json
{
  "id": 1,
  "name": "Sakthi",
  "email": "sakthi@example.com"
}
```

---

### 2. Create Categories
- **Endpoint**: `POST /api/categories`
- **Request Body**:
```json
{
  "name": "Food"
}
```
- **Response** (`201 CREATED`):
```json
{
  "id": 1,
  "name": "Food"
}
```

---

### 3. Set Monthly Budget
- **Endpoint**: `POST /api/budgets`
- **Request Body**:
```json
{
  "userId": 1,
  "categoryId": 1,
  "amount": 5000.00,
  "month": "2026-09"
}
```
- **Response** (`201 CREATED`):
```json
{
  "id": 1,
  "amount": 5000.0,
  "month": "2026-09",
  "user": {
    "id": 1,
    "name": "Sakthi",
    "email": "sakthi@example.com"
  },
  "category": {
    "id": 1,
    "name": "Food"
  }
}
```

---

### 4. Add Expense — Normal (Below 90% Threshold)
- **Endpoint**: `POST /api/expenses`
- **Request Body**:
```json
{
  "userId": 1,
  "categoryId": 1,
  "amount": 3000.00,
  "date": "2026-09-10"
}
```
- **Response** (`201 CREATED`):
```json
{
  "message": "Expense added successfully",
  "category": "Food",
  "totalSpent": 3000.0,
  "budget": 5000.0,
  "percentageUsed": 60.0,
  "alert": false
}
```

---

### 5. Add Expense — Budget Alert Exceeded ($\ge 90\%$)
- **Endpoint**: `POST /api/expenses`
- **Request Body**:
```json
{
  "userId": 1,
  "categoryId": 1,
  "amount": 1600.00,
  "date": "2026-09-28"
}
```
- **Response** (`201 CREATED`):
```json
{
  "message": "Budget alert",
  "category": "Food",
  "totalSpent": 4600.0,
  "budget": 5000.0,
  "percentageUsed": 92.0,
  "alert": true
}
```

---

### 6. View Current Month Category Spending Summary
- **Endpoint**: `GET /api/expenses/summary/current-month/1`
- **Response** (`200 OK`):
```json
[
  {
    "category": "Food",
    "totalSpent": 4600.0
  },
  {
    "category": "Travel",
    "totalSpent": 1800.0
  },
  {
    "category": "Stationery",
    "totalSpent": 750.0
  }
]
```

---

### 7. View Month-over-Month Spending Trend
- **Endpoint**: `GET /api/expenses/trend/1`
- **Response** (`200 OK`):
```json
[
  {
    "month": "2026-07",
    "totalSpent": 6200.0
  },
  {
    "month": "2026-08",
    "totalSpent": 7100.0
  },
  {
    "month": "2026-09",
    "totalSpent": 7150.0
  }
]
```

---

## 12. Error Handling & Edge Cases

### Mandatory Edge Case: Negative Expense Amount
- **Request**:
```json
{
  "userId": 1,
  "categoryId": 1,
  "amount": -100.00,
  "date": "2026-09-28"
}
```
- **Response** (`400 BAD_REQUEST`):
```json
{
  "error": "Expense amount must be greater than zero"
}
```

### Mandatory Edge Case: Zero Expense Amount
- **Request**:
```json
{
  "userId": 1,
  "categoryId": 1,
  "amount": 0.00,
  "date": "2026-09-28"
}
```
- **Response** (`400 BAD_REQUEST`):
```json
{
  "error": "Expense amount must be greater than zero"
}
```

### Resource Not Found
- **Request**: `GET /api/users/999`
- **Response** (`404 NOT_FOUND`):
```json
{
  "error": "User not found with id: 999"
}
```
