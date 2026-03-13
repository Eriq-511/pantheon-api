<#
.SYNOPSIS
    Starts the test PostgreSQL container (if not already running) and executes
    the full Maven test suite against it.

.EXAMPLE
    .\run-tests.ps1
    .\run-tests.ps1 -SkipDocker   # skip container management, DB must already be up
#>
param(
    [switch]$SkipDocker
)

$ErrorActionPreference = 'Continue'

$BACKEND_DIR = Split-Path $PSScriptRoot -Parent   # backend/

if (-not $SkipDocker) {
    # Delegate container lifecycle to start-db.ps1
    & "$PSScriptRoot\start-db.ps1"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

# ── Run Maven tests ────────────────────────────────────────────────────────────
Set-Location $BACKEND_DIR
Write-Host "`n[INFO] Running .\mvnw clean test ..." -ForegroundColor Cyan
cmd /c ".\mvnw clean test"
$mvnExit = $LASTEXITCODE

if ($mvnExit -eq 0) {
    Write-Host "`n[INFO] All tests PASSED." -ForegroundColor Green
} else {
    Write-Host "`n[ERROR] Tests FAILED - see output above." -ForegroundColor Red
}

exit $mvnExit
