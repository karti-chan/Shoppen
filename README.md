# Shoppen (Radnom-App) 🛒

A modern e-commerce SPA (Single Page Application) built with a client-server architecture. It features a reactive frontend written in **React + Vite** and a secure, high-performance backend powered by **Spring Boot**.

## 🚀 Technologies

### Backend
* **Java 17**
* **Spring Boot 3.2.0**
  * **Spring Web** – REST API development.
  * **Spring Security & JWT (JSON Web Tokens)** – User authentication & authorization (including Refresh Tokens).
  * **Spring Data JPA** – Database persistence.
  * **Spring Boot Mail** – Email notification services (e.g. password resets).
  * **Thymeleaf** – Email templates.
* **Database:** Supports H2 (for development) and MySQL (production).
* **Lombok** – Generates boilerplate code (getters/setters, builders, etc.).

### Frontend
* **React 19**
* **Vite** – Fast build tooling and dev server.
* **React Router DOM** – Application routing.
* **Axios** – HTTP client with custom interceptors for automatic JWT refreshing.
* **JS Cookie** – Safe storage of client-side tokens.

---

## 🌟 Features

1. **Authentication & Authorization System:**
   - User sign-up and login.
   - Stateless **JWT** authorization to protect secured endpoints.
   - **Refresh Token** mechanism to keep users logged in securely.
   - Password recovery via one-time tokens sent to the user's email.
2. **Catalog & Product Browsing:**
   - View list of products and product details.
   - Dynamic search filter.
3. **Shopping Cart:**
   - Add, update quantities, and remove products from the cart.
   - User cart persistence synchronized directly with the database.
4. **Environment-Specific Mail Service Configuration:**
   - Differentiated mail behaviors based on active profiles (`Dev`, `Sandbox`, `Prod`).

---

## 🛠️ Getting Started

### Prerequisites
* **JDK 17** or higher installed.
* **Node.js** installed (LTS version recommended).
* **Maven** (or use the included Maven wrapper `mvnw`).

### Step 1: Clone the Repository
```bash
git clone https://github.com/karti-chan/Shoppen.git
cd Shoppen
```

### Step 2: Run the Backend (Spring Boot)
1. Create an `application.properties` (or `application-dev.properties`) configuration file in `src/main/resources/` if it does not exist. Example configuration using H2:
   ```properties
   spring.datasource.url=jdbc:h2:file:./data/radnom_db
   spring.datasource.driverClassName=org.h2.Driver
   spring.datasource.username=sa
   spring.datasource.password=
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   spring.h2.console.enabled=true
   ```
2. Start the application:
   ```bash
   # On Windows:
   mvnw.cmd spring-boot:run
   
   # On Linux/macOS:
   ./mvnw spring-boot:run
   ```
The backend server will start on port `8080` by default.

### Step 3: Run the Frontend (React)
1. Navigate to the frontend directory:
   ```bash
   cd src/main/frontend
   ```
2. Install the dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
The frontend application will be available at `http://localhost:5173/`.

---

## 📁 Project Structure

```text
├── .gitignore
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/radnom/
│   │   │   ├── config/       # Security, JWT, CORS configurations
│   │   │   ├── controller/   # REST API Controllers
│   │   │   ├── entity/       # JPA Entities / Models
│   │   │   ├── exception/    # Global Exception Handlers
│   │   │   ├── repository/   # JPA Repositories
│   │   │   └── service/      # Business Logic Services
│   │   ├── resources/
│   │   │   ├── templates/    # Thymeleaf templates
│   │   │   └── data.sql      # Database initialization script
│   │   └── frontend/         # React + Vite frontend application
│   └── test/                 # Backend unit & integration tests
```
