---
description: Start Hall Ticket project with local MySQL (password: pathpair)
---

1. **Ensure MySQL is running**
   - If you have Docker installed, you can start the MySQL container:
   ```bash
   docker compose up -d
   ```
   - Alternatively, start your local MySQL service via Windows Services or MySQL Workbench.

2. **Create the database (if not already created)**
   ```bash
   mysql -u root -ppathpair -e "CREATE DATABASE IF NOT EXISTS hall_ticket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   ```
   - You can also use a MySQL client GUI to run the above statement.

3. **Configure application properties**
   - Copy the example properties file:
   ```bash
   cp application-local.properties.example src/main/resources/application-local.properties
   ```
   - Ensure the following lines are set (they already contain the correct password):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/hall_ticket
   spring.datasource.username=root
   spring.datasource.password=pathpair
   ```
   - If you prefer to use the default `application.properties`, it already defaults to `root` / `pathpair`.

4. **Build the project** (optional but recommended)
   ```bash
   ./mvnw clean package
   ```
   - On Windows use `mvnw.cmd clean package`.

5. **Run the application**
   - Development mode (hot reload):
   ```bash
   ./mvnw spring-boot:run
   ```
   - On Windows use `mvnw.cmd spring-boot:run`.
   - Production mode (using the built JAR):
   ```bash
   java -jar target/Hall_Ticket-0.0.1-SNAPSHOT.jar
   ```

6. **Verify**
   - Open a browser and navigate to `http://localhost:8080`.
   - Access the admin login at `http://localhost:8080/AdminLoginPage`.
   - Check the MySQL database to see tables created (`admin_model`, `student_model`, etc.).

**Notes**
- The `application.properties` already uses `${DB_PASSWORD:pathpair}` as the default, so no further changes are needed if you keep the defaults.
- If you encounter port conflicts, adjust the MySQL port in `docker-compose.yml` or stop other MySQL services.
- To stop the Docker container: `docker compose down`.
