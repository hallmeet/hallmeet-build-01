@echo off
echo ========================================
echo HallMeet - Production Mode
echo ========================================
echo.
echo Building application...
echo.

call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Build successful! Starting application...
    echo ========================================
    echo.
    echo Application will be available at:
    echo   - Home: http://localhost:8080
    echo   - Admin: http://localhost:8080/AdminLoginPage
    echo   - Student: http://localhost:8080/signin_page
    echo.
    echo Press Ctrl+C to stop the application
    echo.
    echo ========================================
    echo.
    java -jar target\Hall_Ticket-0.0.1-SNAPSHOT.jar
) else (
    echo.
    echo ========================================
    echo Build failed! Please check the errors above.
    echo ========================================
    echo.
)

pause


