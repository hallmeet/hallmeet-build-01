@echo off
echo ========================================
echo HallMeet - Database Setup
echo ========================================
echo.
echo This script will help you set up the MySQL database.
echo.
echo Prerequisites:
echo   1. MySQL Server must be installed and running
echo   2. You need MySQL root password
echo.
echo ========================================
echo.

set /p MYSQL_ROOT_PASSWORD="Enter MySQL root password: "

echo.
echo Creating database 'hall_ticket'...
echo.

mysql -u root -p%MYSQL_ROOT_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS hall_ticket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if %ERRORLEVEL% EQU 0 (
    echo Database 'hall_ticket' created successfully!
    echo.
    
    echo Creating user 'halluser'...
    echo.
    
    mysql -u root -p%MYSQL_ROOT_PASSWORD% -e "CREATE USER IF NOT EXISTS 'halluser'@'localhost' IDENTIFIED BY 'hallpass123';"
    mysql -u root -p%MYSQL_ROOT_PASSWORD% -e "GRANT ALL PRIVILEGES ON hall_ticket.* TO 'halluser'@'localhost';"
    mysql -u root -p%MYSQL_ROOT_PASSWORD% -e "FLUSH PRIVILEGES;"
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ========================================
        echo Database setup completed successfully!
        echo ========================================
        echo.
        echo Database: hall_ticket
        echo Username: halluser
        echo Password: hallpass123
        echo.
        echo Note: Tables will be created automatically
        echo       when you run the application.
        echo.
        echo ========================================
    ) else (
        echo.
        echo Error creating user. Please check MySQL permissions.
        echo You can still use root user by updating application.properties
    )
) else (
    echo.
    echo ========================================
    echo Error creating database!
    echo ========================================
    echo.
    echo Possible issues:
    echo   1. MySQL root password is incorrect
    echo   2. MySQL service is not running
    echo   3. MySQL is not in PATH
    echo.
    echo You can manually create the database:
    echo   1. Open MySQL Command Line Client
    echo   2. Run: CREATE DATABASE hall_ticket;
    echo.
)

echo.
pause


