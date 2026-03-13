<#
.SYNOPSIS
    Start the dev postgres container, then run the Spring Boot backend locally.
    Reads secrets from backend/.env so they are injected as environment variables.

.DESCRIPTION
    1. Reads backend/.env and sets each KEY=VALUE as a process-scoped env var.
    2. Starts (or verifies) the postgres Docker service via docker compose.
    3. Runs `mvnw spring-boot:run` so the backend connects to localhost:5432.

.EXAMPLE
    # From the backend/ directory:
    .\scripts\start-local.ps1
#>

$ErrorActionPreference = 'Stop'

# ── Paths ────────────────────────────────────────────────────────────────────
$scriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Resolve-Path (Join-Path $scriptDir '..')
$envFile    = Join-Path $backendDir '.env'

Set-Location $backendDir

# ── 1. Load .env ─────────────────────────────────────────────────────────────
if (Test-Path $envFile) {
    Write-Host "[INFO] Loading environment from $envFile" -ForegroundColor Cyan
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        # Skip blank lines and comments
        if ($line -eq '' -or $line.StartsWith('#')) { return }
        $idx = $line.IndexOf('=')
        if ($idx -lt 1) { return }
        $key   = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        [System.Environment]::SetEnvironmentVariable($key, $value, 'Process')
        Write-Host "  SET $key" -ForegroundColor DarkCyan
    }
} else {
    Write-Host "[WARN] No .env file found at $envFile — using application.properties defaults." -ForegroundColor Yellow
}

# ── 2. Start postgres via docker compose ─────────────────────────────────────
Write-Host "`n[INFO] Starting postgres container..." -ForegroundColor Cyan
docker compose up -d postgres
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] docker compose failed. Is Docker Desktop running?" -ForegroundColor Red
    exit 1
}

# Wait until postgres is healthy
Write-Host "[INFO] Waiting for postgres to be ready..." -ForegroundColor Cyan
$maxWait = 30
$waited  = 0
do {
    Start-Sleep -Seconds 2
    $waited += 2
    $status = docker inspect --format '{{.State.Health.Status}}' pantheon 2>$null
} while ($status -ne 'healthy' -and $waited -lt $maxWait)

if ($status -ne 'healthy') {
    Write-Host "[ERROR] Postgres did not become healthy within $maxWait seconds." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Postgres is healthy." -ForegroundColor Green

# ── 3. Run Spring Boot ────────────────────────────────────────────────────────
Write-Host "`n[INFO] Starting Spring Boot backend (localhost:8080)..." -ForegroundColor Cyan
Write-Host "       Press Ctrl+C to stop.`n" -ForegroundColor DarkGray

.\mvnw spring-boot:run -f pom.xml
