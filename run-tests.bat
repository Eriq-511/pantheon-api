@echo off
setlocal

rem Runs backend test suite and ensures the Postgres test container is running.
rem Delegates to PowerShell for Docker lifecycle + Maven wrapper invocation.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\run-tests.ps1" %*
exit /b %ERRORLEVEL%
