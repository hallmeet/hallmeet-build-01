# Hall Ticket Generation System - Setup Guide

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Detailed Setup](#detailed-setup)
- [Development Mode](#development-mode)
- [Production Mode](#production-mode)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before setting up the project, ensure you have the following installed:

### Required Software
- **Java Development Kit (JDK) 21** - [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Docker & Docker Compose** - [Download](https://www.docker.com/products/docker-desktop)
- **Maven 3.6+** (Optional - Maven wrapper included)

### Verify Installation
```bash
# Check Java version
java -version  # Should show Java 21

# Check Java compiler
javac -version  # Should show Java 21

# Check Docker
docker --version
docker compose version
```

---

## Quick Start

### Option 0: Run without MySQL (zero config)

If you **don't want to use MySQL** (e.g. port 3306 is already in use, or you haven't set up `halluser`), run with the **H2 in-memory** profile. No database setup needed; data is lost when the app stops.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Then open **http://localhost:8080**. Optional: H2 console at http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:hall_ticket`, user: `sa`, password: leave empty).

---

### Use MySQL (persistent storage)

To store data in a **real database** (visible in DBeaver, persistent across restarts):

1. **Have MySQL running**  
   - **Docker** (if port 3306 is free): `docker compose up -d` in the project root.  
   - **Local MySQL**: Ensure the MySQL service is running.

2. **Create database and user** (if not using Docker)  
   - Linux (e.g. Ubuntu): `sudo mysql`, then run:
     ```sql
     CREATE DATABASE IF NOT EXISTS hall_ticket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     CREATE USER IF NOT EXISTS 'halluser'@'localhost' IDENTIFIED BY 'hallpass123';
     GRANT ALL PRIVILEGES ON hall_ticket.* TO 'halluser'@'localhost';
     FLUSH PRIVILEGES;
     EXIT;
     ```
   - Windows / other: Use MySQL Workbench or `mysql -u root -p` and run the same SQL.

3. **Run the app without the H2 profile**  
   - Do **not** use `-Dspring-boot.run.profiles=h2`.
   - Start with: `./mvnw spring-boot:run` (or run from IDE with no active profile).
   - The app will connect to MySQL; JPA creates/updates tables automatically.

4. **Optional: override connection**  
   - Defaults: host `localhost`, port `3306`, database `hall_ticket`, user `halluser`, password `hallpass123`.  
   - Override with environment variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.  
   - Example (root): `export DB_USERNAME=root` and `export DB_PASSWORD=your_mysql_root_password`.

5. **First-time admin**  
   - With MySQL, an initial admin user is seeded automatically (see `data.sql`).  
   - Or insert manually: `INSERT INTO admin_model (email, full_name, mobile_no, password, role) VALUES ('swapnil@gmail.com', 'swapnil', '1234567890', '123456789', 'admin');`

**Verify:** In DBeaver, connect to MySQL `localhost:3306`, database `hall_ticket`, user `halluser`. After using the app, you should see data in `admin_model`, `hall_model`, `student_model`, `hall_ticket_model`, etc.

---

### Option 1: Using Quick Start Scripts (Recommended)

#### Linux/Mac:
```bash
# Make scripts executable
chmod +x start.sh dev.sh stop.sh

# Start in Production Mode
./start.sh

# OR Start in Development Mode (with Hot Reload)
./dev.sh

# Stop the application
./stop.sh
```

#### Windows:
```powershell
# Start in Production Mode
start.bat

# OR Start in Development Mode
# Use: mvnw.cmd spring-boot:run
```

### Option 2: Manual Setup

#### Step 1: Start MySQL Database
```bash
# Start MySQL using Docker Compose
docker compose up -d

# Verify MySQL is running
docker ps
```

#### Step 2: Build the Project
```bash
# Make Maven wrapper executable (Linux/Mac)
chmod +x mvnw

# Build the project
./mvnw clean package

# Or on Windows
mvnw.cmd clean package
```

#### Step 3: Run the Application
```bash
# Production Mode (JAR file)
java -jar target/Hall_Ticket-0.0.1-SNAPSHOT.jar

# Development Mode (with Hot Reload)
./mvnw spring-boot:run
```

#### Step 4: Access the Application
Open your browser and navigate to:
- **Application**: http://localhost:8080
- **Admin Login**: http://localhost:8080/AdminLoginPage
- **Student Login**: http://localhost:8080/signin_page

---

## Detailed Setup

### 1. Database Setup

#### Using Docker (Recommended)
```bash
# Start MySQL container
docker compose up -d

# Check container status
docker ps

# View logs if needed
docker logs hall_ticket_mysql
```

**Default MySQL Credentials:**
- **Host**: localhost:3306
- **Database**: hall_ticket
- **Username**: root
- **Password**: pathpair
- **(Alternative user)**: halluser / hallpass123
- **Root Password**: hallticket123

#### Using Local MySQL
1. Create a database named `hall_ticket`
2. Update `application.properties` or set environment variables:
   ```properties
   DB_HOST=localhost
   DB_USERNAME=root
   DB_PASSWORD=your_password
   ```

### 2. Environment Configuration

#### Option A: Environment Variables
```bash
export DB_HOST=localhost
export DB_USERNAME=root        # or halluser
export DB_PASSWORD=pathpair   # or hallpass123
export DB_PORT=3306
export DB_NAME=hall_ticket
```

#### Option B: Application Properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hall_ticket?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=pathpair
```

### 3. Build Configuration

The project uses Maven with the following key configurations:
- **Java Version**: 21
- **Spring Boot**: 3.4.1
- **Build Tool**: Maven (wrapper included)

---

## Development Mode

### Hot Reload Setup

The project includes **Spring Boot DevTools** for automatic hot reload:

#### Features:
- ✅ **Java code changes**: Auto-recompiles and restarts (2-5 seconds)
- ✅ **Template changes**: Auto-reloads (1-2 seconds)
- ✅ **Resource changes**: Auto-reloads (1-2 seconds)
- ✅ **No manual restart needed!**

#### How to Use:
```bash
# Start in development mode
./dev.sh

# Or manually
./mvnw spring-boot:run
```

#### What Gets Hot Reloaded:
- Java files (`src/main/java/**`)
- Templates (`src/main/resources/templates/**`)
- Static resources (`src/main/resources/static/**`)
- Properties files (`src/main/resources/application.properties`)

#### What Does NOT Hot Reload:
- Running the JAR file (`./start.sh`)
- Changes to `pom.xml` (requires rebuild)
- Database schema changes (requires restart)

---

## Production Mode

### Building for Production
```bash
# Clean and build
./mvnw clean package

# The JAR file will be created at:
# target/Hall_Ticket-0.0.1-SNAPSHOT.jar
```

### Running Production Build
```bash
# Using start script
./start.sh

# Or manually
java -jar target/Hall_Ticket-0.0.1-SNAPSHOT.jar
```

### Production Considerations
1. **Database**: Use a production MySQL instance
2. **Environment Variables**: Set production credentials
3. **File Storage**: Configure proper paths for images and QR codes
4. **Logging**: Configure appropriate log levels
5. **Security**: Enable HTTPS and configure security settings

---

## Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:hall_ticket}
spring.datasource.username=${DB_USERNAME:halluser}
spring.datasource.password=${DB_PASSWORD:hallpass123}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Thymeleaf (Templates)
spring.thymeleaf.cache=false  # Set to true in production

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Server Port (optional)
# server.port=8080
```

### Docker Configuration

The `docker-compose.yml` file configures:
- MySQL 8.0 container
- Persistent data volumes
- Health checks
- Network configuration

---

## Troubleshooting

### Common Issues

#### 1. Java Version Error
**Error**: `release version 25 not supported` or similar
**Solution**: 
- Ensure Java 21 is installed
- Check `JAVA_HOME` environment variable
- Verify with `java -version`

#### 2. Database Connection Failed
**Error**: `Communications link failure`
**Solution**:
- Ensure MySQL container is running: `docker ps`
- Check database credentials in `application.properties`
- Verify port 3306 is not in use
- Check Docker logs: `docker logs hall_ticket_mysql`

#### 3. Port 3306 Already in Use / Docker MySQL Won't Start
**Error**: `failed to bind host port 0.0.0.0:3306/tcp: address already in use`
**Cause**: Another MySQL (or service) is already using port 3306 on your machine.
**Solutions**:
- **Run without MySQL**: use the H2 profile so the app doesn't need MySQL:
  ```bash
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
  ```
- **Or use your existing MySQL**: create the database and app user (see [Use existing local MySQL](#use-existing-local-mysql) in Detailed Setup), then run `./mvnw spring-boot:run` (no profile). Don't start the Docker MySQL container.

#### 4. Access denied for user 'halluser'@'localhost'
**Error**: `Access denied for user 'halluser'@'localhost' (using password: YES)`
**Cause**: The app is connecting to a MySQL on port 3306 (usually your **local** MySQL) that doesn't have user `halluser` or the password doesn't match.
**Solutions**:
- **Quick fix**: run with H2 (no MySQL): `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`
- **Use MySQL**: create database and user in your local MySQL:
  ```sql
  CREATE DATABASE IF NOT EXISTS hall_ticket;
  CREATE USER IF NOT EXISTS 'halluser'@'localhost' IDENTIFIED BY 'hallpass123';
  GRANT ALL ON hall_ticket.* TO 'halluser'@'localhost';
  FLUSH PRIVILEGES;
  ```
- Or set env vars to your MySQL credentials: `DB_USERNAME=root DB_PASSWORD=your_mysql_password ./mvnw spring-boot:run`

#### 4b. ERROR 1698: Access denied for user 'root'@'localhost' (Linux)
**Error**: `ERROR 1698 (28000): Access denied for user 'root'@'localhost'` when running `mysql -u root -p`
**Cause**: On Ubuntu/Debian, MySQL often uses the **auth_socket** plugin for root, so root can only log in as the system user, not with a password.
**Solution**: Use **sudo** to open the MySQL shell, then create the database and app user:
```bash
sudo mysql
```
Inside the MySQL prompt:
```sql
CREATE DATABASE IF NOT EXISTS hall_ticket;
CREATE USER IF NOT EXISTS 'halluser'@'localhost' IDENTIFIED BY 'hallpass123';
GRANT ALL ON hall_ticket.* TO 'halluser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```
Then run the app (no profile): `./mvnw spring-boot:run`.  
**Alternative**: Run without MySQL: `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`

#### 5. Port 8080 Already in Use
**Error**: `Port 8080 is already in use`
**Solution**:
- Change port in `application.properties`: `server.port=8081`
- Or stop the process using port 8080

#### 6. Maven Build Fails
**Error**: Build compilation errors
**Solution**:
- Clean and rebuild: `./mvnw clean package`
- Check Java version: `java -version`
- Verify `pom.xml` is correct

#### 7. Hot Reload Not Working
**Solution**:
- Ensure you're using `./dev.sh` or `./mvnw spring-boot:run`
- Hot reload doesn't work with JAR file execution
- Check DevTools is in `pom.xml` dependencies

#### 8. File Upload Issues
**Error**: File size too large
**Solution**:
- Increase limits in `application.properties`:
  ```properties
  spring.servlet.multipart.max-file-size=20MB
  spring.servlet.multipart.max-request-size=20MB
  ```

### Getting Help

1. Check application logs in console output
2. Review Docker logs: `docker logs hall_ticket_mysql`
3. Verify all prerequisites are installed
4. Check network connectivity to database
5. Review `application.properties` configuration

---

## Project Structure

```
Hall_Ticket/
├── src/
│   ├── main/
│   │   ├── java/              # Java source code
│   │   │   └── com/example/HAllTicket/
│   │   │       ├── controller/    # Controllers
│   │   │       ├── model/          # Entity models
│   │   │       ├── repository/     # Data repositories
│   │   │       ├── service/        # Business logic
│   │   │       └── config/         # Configuration
│   │   └── resources/
│   │       ├── templates/      # Thymeleaf templates
│   │       ├── static/         # Static resources (CSS, JS, images)
│   │       └── application.properties
│   └── test/                   # Test files
├── docker/                     # Docker configuration
├── docker-compose.yml          # Docker Compose setup
├── pom.xml                     # Maven configuration
├── mvnw                        # Maven wrapper (Linux/Mac)
├── mvnw.cmd                    # Maven wrapper (Windows)
├── start.sh                    # Production start script
├── dev.sh                      # Development start script
└── stop.sh                     # Stop script
```

---

## Next Steps

After successful setup:
1. Access the application at http://localhost:8080
2. Create an admin account (if needed)
3. Register student accounts
4. Create exams and generate hall tickets
5. Review [PROJECT_DETAILS.md](PROJECT_DETAILS.md) for detailed information

---

## Support

For issues or questions:
- Review the troubleshooting section above
- Check application logs
- Verify all prerequisites are met
- Review project documentation in `PROJECT_DETAILS.md`


