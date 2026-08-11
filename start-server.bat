@echo off
cd /d "C:\Users\Shree\Documents\New OpenCode Project\backend"
REM Load environment variables from .env (gitignored) into this process
if exist ".env" (
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do set "%%a=%%b"
)
java -jar target\user-registration-backend-1.0.0.jar >> startup.log 2>&1
