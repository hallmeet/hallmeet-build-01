# HallMeet - Windows 11 Setup Guide

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Step-by-Step Setup](#step-by-step-setup)
- [Database Configuration](#database-configuration)
- [Running the Application](#running-the-application)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 21**
   - Download from: https://www.oracle.com/java/technologies/downloads/#java21
   - Or use OpenJDK: https://adoptium.net/
   - **Important**: Install JDK (not just JRE)
   - Verify installation:
     ```cmd
     java -version
     javac -version
     ```
   - Both should show version 21

2. **MySQL Server 8.0 or higher**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - During installation, remember your root password
   - Make sure MySQL service is running

3. **Maven (Optional)**
   - The project includes Maven wrapper (`mvnw.cmd`), so Maven installation is optional
   - If you want to install Maven: https://maven.apache.org/download.cgi

### Verify Prerequisites

Open Command Prompt or PowerShell and run:

```cmd
java -version
javac -version
mysql --version
```

All commands should execute without errors.

---

## Step-by-Step Setup

### Step 1: Clone/Extract the Project

1. Extract the project to a folder (e.g., `C:\Projects\Hall_Ticket`)
2. Open Command Prompt or PowerShell in the project directory

### Step 2: Create MySQL Database

1. **Open MySQL Command Line Client** or use MySQL Workbench

2. **Login to MySQL**:
   ```sql
   mysql -u root -p
   ```
   Enter your MySQL root password when prompted

3. **Create the Database**:
   ```sql
   CREATE DATABASE hall_ticket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

4. **Create a User (Optional but Recommended)**:
   ```sql
   CREATE USER 'halluser'@'localhost' IDENTIFIED BY 'hallpass123';
   GRANT ALL PRIVILEGES ON hall_ticket.* TO 'halluser'@'localhost';
   FLUSH PRIVILEGES;
   ```

5. **Verify Database Creation**:
   ```sql
   SHOW DATABASES;
   ```
   You should see `hall_ticket` in the list

6. **Exit MySQL**:
   ```sql
   EXIT;
   ```

### Step 3: Configure Database Connection

The project uses **automatic table creation** via JPA/Hibernate. You only need to configure the database connection.

#### Option A: Using Environment Variables (Recommended)

1. **Open System Environment Variables**:
   - Press `Win + R`, type `sysdm.cpl`, press Enter
   - Go to "Advanced" tab → Click "Environment Variables"

2. **Add User Variables**:
   - Click "New" under "User variables"
   - Add the following variables:

   ```
   Variable Name: DB_HOST
   Variable Value: localhost
   ```

   ```
   Variable Name: DB_PORT
   Variable Value: 3306
   ```

   ```
   Variable Name: DB_NAME
   Variable Value: hall_ticket
   ```

   ```
   Variable Name: DB_USERNAME
   Variable Value: halluser
   ```

   ```
   Variable Name: DB_PASSWORD
   Variable Value: hallpass123
   ```

   **Note**: If you used different credentials in Step 2, use those values instead.

3. **Restart Command Prompt/PowerShell** after setting environment variables

#### Option B: Edit application.properties Directly

1. Open `src/main/resources/application.properties`

2. Update the database configuration:
   ```properties
   # For root user
   spring.datasource.url=jdbc:mysql://localhost:3306/hall_ticket?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_ROOT_PASSWORD
   ```

   OR if you created the `halluser`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/hall_ticket?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=halluser
   spring.datasource.password=hallpass123
   ```

### Step 4: Build the Project

Open Command Prompt or PowerShell in the project directory and run:

```cmd
mvnw.cmd clean package -DskipTests
```

This will:
- Download Maven dependencies (first time only, may take a few minutes)
- Compile the project
- Create a JAR file in the `target` folder

**Expected Output**: `BUILD SUCCESS`

### Step 5: Run the Application

#### Option A: Run JAR File (Production Mode)

```cmd
java -jar target\Hall_Ticket-0.0.1-SNAPSHOT.jar
```

#### Option B: Run with Maven (Development Mode with Hot Reload)

```cmd
mvnw.cmd spring-boot:run
```

**Expected Output**: You should see:
```
Started HallTicketApplication in X.XXX seconds
```

### Step 6: Verify Tables Are Created

1. **Open MySQL Command Line**:
   ```cmd
   mysql -u root -p
   ```

2. **Select the Database**:
   ```sql
   USE hall_ticket;
   ```

3. **List Tables**:
   ```sql
   SHOW TABLES;
   ```

You should see tables like:
- `admin_model`
- `student_model`
- `hall_model`
- `college_model`
- `syllabus_model`
- `exam_model`
- `hall_ticket_model`

**Note**: Tables are created automatically on first run. If tables don't appear, check the application logs for errors.

### Step 7: Access the Application

Open your web browser and navigate to:

- **Home Page**: http://localhost:8080
- **Admin Login**: http://localhost:8080/AdminLoginPage
- **Student Login**: http://localhost:8080/signin_page
- **Registration**: http://localhost:8080/registration

---

## Database Configuration

### Automatic Table Creation

The project uses **JPA/Hibernate** with `ddl-auto=update`, which means:

✅ **Tables are created automatically** when the application starts  
✅ **Tables are updated automatically** if entity models change  
✅ **No migration scripts needed**  
✅ **Data is preserved** when tables are updated

### Configuration Details

In `application.properties`:
```properties
# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

**What this means**:
- `ddl-auto=update`: Creates/updates tables based on entity models
- Tables are created on first run
- Schema changes are applied automatically on subsequent runs
- Existing data is preserved

### Database Tables

The following tables are created automatically:

1. **admin_model** - Admin user accounts
2. **student_model** - Student information
3. **hall_model** - General user accounts
4. **college_model** - College information
5. **syllabus_model** - Course syllabus
6. **exam_model** - Exam details
7. **hall_ticket_model** - Hall ticket records

---

## Running the Application

### Development Mode (Recommended for Development)

```cmd
mvnw.cmd spring-boot:run
```

**Features**:
- Hot reload enabled (code changes auto-reload)
- Detailed error messages
- Faster startup

### Production Mode

```cmd
mvnw.cmd clean package -DskipTests
java -jar target\Hall_Ticket-0.0.1-SNAPSHOT.jar
```

**Features**:
- Optimized performance
- Single JAR file
- Suitable for deployment

### Using Batch Scripts

You can create batch files for easier execution:

**`start-dev.bat`** (Development Mode):
```batch
@echo off
echo Starting HallMeet in Development Mode...
mvnw.cmd spring-boot:run
pause
```

**`start-prod.bat`** (Production Mode):
```batch
@echo off
echo Building and starting HallMeet...
call mvnw.cmd clean package -DskipTests
if %ERRORLEVEL% EQU 0 (
    echo Starting application...
    java -jar target\Hall_Ticket-0.0.1-SNAPSHOT.jar
) else (
    echo Build failed!
)
pause
```

---

## Troubleshooting

### Issue 1: Java Not Found

**Error**: `'java' is not recognized as an internal or external command`

**Solution**:
1. Install JDK 21
2. Add Java to PATH:
   - Find Java installation (usually `C:\Program Files\Java\jdk-21`)
   - Add to PATH: `C:\Program Files\Java\jdk-21\bin`
3. Restart Command Prompt

### Issue 2: MySQL Connection Failed

**Error**: `Communications link failure` or `Access denied`

**Solution**:
1. **Verify MySQL is running**:
   ```cmd
   net start MySQL80
   ```
   Or check Services: `Win + R` → `services.msc` → Find "MySQL80"

2. **Check credentials** in `application.properties` or environment variables

3. **Test MySQL connection**:
   ```cmd
   mysql -u root -p
   ```

4. **Verify database exists**:
   ```sql
   SHOW DATABASES;
   ```

5. **Check firewall** - Ensure port 3306 is not blocked

### Issue 3: Port 8080 Already in Use

**Error**: `Port 8080 is already in use`

**Solution**:
1. **Find process using port 8080**:
   ```cmd
   netstat -ano | findstr :8080
   ```

2. **Kill the process** (replace PID with actual process ID):
   ```cmd
   taskkill /PID <PID> /F
   ```

3. **Or change port** in `application.properties`:
   ```properties
   server.port=8081
   ```

### Issue 4: Tables Not Created

**Error**: Tables don't appear in database

**Solution**:
1. **Check application logs** for database errors
2. **Verify database connection** is correct
3. **Check user permissions**:
   ```sql
   SHOW GRANTS FOR 'halluser'@'localhost';
   ```
4. **Manually verify** `ddl-auto=update` is set in `application.properties`
5. **Restart application** - tables are created on startup

### Issue 5: Maven Build Fails

**Error**: Build compilation errors

**Solution**:
1. **Clean and rebuild**:
   ```cmd
   mvnw.cmd clean package -DskipTests
   ```

2. **Check Java version**:
   ```cmd
   java -version
   ```
   Must be Java 21

3. **Delete Maven cache** (if needed):
   ```cmd
   rmdir /s %USERPROFILE%\.m2\repository
   ```

4. **Re-download dependencies**:
   ```cmd
   mvnw.cmd clean install -U
   ```

### Issue 6: Environment Variables Not Working

**Error**: Application uses wrong database credentials

**Solution**:
1. **Restart Command Prompt/PowerShell** after setting environment variables
2. **Verify variables are set**:
   ```cmd
   echo %DB_USERNAME%
   echo %DB_PASSWORD%
   ```
3. **Use application.properties** instead if environment variables don't work

### Issue 7: Hot Reload Not Working

**Solution**:
- Hot reload only works with `mvnw.cmd spring-boot:run`
- It does NOT work with JAR file execution
- Ensure DevTools is in dependencies (already included)

---

## Quick Reference

### Common Commands

```cmd
# Build project
mvnw.cmd clean package -DskipTests

# Run in development mode
mvnw.cmd spring-boot:run

# Run JAR file
java -jar target\Hall_Ticket-0.0.1-SNAPSHOT.jar

# Check MySQL status
net start MySQL80

# Connect to MySQL
mysql -u root -p

# Check Java version
java -version
javac -version
```

### Default Credentials

**Database** (if using created user):
- Username: `halluser`
- Password: `hallpass123`
- Database: `hall_ticket`

**Application URLs**:
- Home: http://localhost:8080
- Admin Login: http://localhost:8080/AdminLoginPage
- Student Login: http://localhost:8080/signin_page

---

## Next Steps

After successful setup:

1. ✅ Access the application at http://localhost:8080
2. ✅ Create an admin account (if needed)
3. ✅ Register student accounts
4. ✅ Create colleges and syllabus
5. ✅ Create exams
6. ✅ Generate hall tickets with QR codes

---

## Support

For additional help:
- Check application logs in the console
- Review `SETUP.md` for general setup information
- Review `PROJECT_DETAILS.md` for project documentation
- Verify all prerequisites are installed correctly

---

## Notes

- **No Docker Required**: This setup uses local MySQL installation
- **Automatic Table Creation**: Tables are created automatically via JPA
- **No Migration Scripts**: Schema changes are handled automatically
- **Data Persistence**: Data is preserved when application restarts
- **Hot Reload**: Available in development mode for faster development


