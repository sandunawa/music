# MelodyMart: Web-Based Music Store & Rental System

MelodyMart is a production-ready, international-standard web application designed as a Spring Boot MVC project for a university software engineering course (SE2030). It features a sleek, responsive dark-themed user interface inspired by Spotify, a robust session-based custom authentication guard, and a thread-safe JSON file-based database architecture.

---

## Technical Architecture & Highlights
1. **Spring Boot (3.3.4)**: Enterprise MVC architecture with complete separation of concerns (Controllers, Services, Repositories, Models).
2. **File-based JSON Database**: Highly portable storage under the `data/` folder. Persisted via Jackson serialization. Uses `ReentrantReadWriteLock` for concurrent read/write thread safety.
3. **Custom Auth Interceptor**: Session-based security guards protecting paths annotated with custom `@LoginRequired` and `@AdminRequired` annotations.
4. **Spotify-inspired Dark Theme**: Built with responsive CSS Grid, Flexbox, custom variables, and native media queries for high-quality printing.
5. **Dynamic Cart Actions**: REST API AJAX bindings in Vanilla JS for immediate quantity adjustments and floating Toast notifications.

---

## Pre-Configured Demo Credentials

Upon startup, the system automatically checks and seeds the file database with default accounts and catalog items:

* **Administrator Role**:
  * **Email**: `admin@melodymart.com`
  * **Password**: `admin123`
* **Customer Role**:
  * **Email**: `customer@melodymart.com`
  * **Password**: `customer123`

---

## How to Import & Run the Project

### Prerequisite
* **Java Development Kit (JDK)**: Version 17 or higher (Java 21/24 is fully supported).

### Run from an IDE (Recommended)
1. Open your Java IDE (e.g., **IntelliJ IDEA**, **Eclipse**, or **VS Code**).
2. Select **Open** or **Import Project** and target the root directory containing the `pom.xml` file.
3. Allow the IDE to import the Maven dependencies automatically.
4. Locate `src/main/java/com/melodymart/MelodyMartApplication.java`.
5. Right-click the file and select **Run 'MelodyMartApplication'** or click the run icon.
6. Open your web browser and navigate to: `http://localhost:8080`

### Run from the Command Line (requires Maven)
If you have Maven installed globally on your machine, you can run the following commands in the project root:

1. **Clean and compile the project**:
   ```bash
   mvn clean compile
   ```
2. **Launch the Spring Boot server**:
   ```bash
   mvn spring-boot:run
   ```
3. **Access the application**: Open `http://localhost:8080` in your web browser.

---

## Project Directory & Module Mapping

* [pom.xml](file:///d:/SLIIT/2nd%20Year/2026/SE/Project/pom.xml): Maven build configuration.
* [src/main/resources/application.properties](file:///d:/SLIIT/2nd%20Year/2026/SE/Project/src/main/resources/application.properties): Application configurations.
* [com.melodymart.config](file:///d:/SLIIT/2nd%20Year/2026/SE/Project/src/main/java/com/melodymart/config): Annotations and custom AuthInterceptors.
* [com.melodymart.model](file:///d:/SLIIT/2nd%20Year/2026/SE/Project/src/main/java/com/melodymart/model): Structured POJO models.
* [com.melodymart.service](file:///d:/SLIIT/2nd%20Year/2026/SE/Project/src/main/java/com/melodymart/service): Services (Persistence, Business logic, Tax calculations).
* [com.melodymart.controller](file:///d:/SLIIT/2nd%20Year/2026/SE/Project/src/main/java/com/melodymart/controller): MVC routes and AJAX Cart APIs.
* [src/main/resources/templates](file:///d:/SLIIT/2nd%20Year/2026/SE/Project/src/main/resources/templates): Thymeleaf HTML UI pages.
* [src/main/resources/static](file:///d:/SLIIT/2nd%20Year/2026/SE/Project/src/main/resources/static): CSS variables stylesheet and Client JS scripts.
