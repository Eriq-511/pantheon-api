<#
.SYNOPSIS
    Ensures the cms-test-postgres Docker container is running and healthy.
    Starts a fresh container if one is not already up.

.EXAMPLE
    .\start-db.ps1
#>

$ErrorActionPreference = 'Continue'

$CONTAINER = 'cms-test-postgres'
$PG_IMAGE  = 'postgres:16-alpine'
$PG_PORT   = '5433'
$PG_DB     = 'cms_test'
$PG_USER   = 'test'
$PG_PASS   = 'test'

Write-Host "[INFO] Verifying Docker is available ..." -ForegroundColor Cyan
docker info 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Docker is not running. Start Docker Desktop first." -ForegroundColor Red
    exit 1
}

# Check if already running
$running = docker inspect --format '{{.State.Running}}' $CONTAINER 2>$null
if ($running -eq 'true') {
    Write-Host "[INFO] Container '$CONTAINER' is already running." -ForegroundColor Green
} else {
    Write-Host "[INFO] Starting '$CONTAINER' ($PG_IMAGE on port $PG_PORT) ..."
    docker stop $CONTAINER 2>$null | Out-Null
    docker rm   $CONTAINER 2>$null | Out-Null

    docker run -d `
        --name $CONTAINER `
        -e POSTGRES_DB=$PG_DB `
        -e POSTGRES_USER=$PG_USER `
        -e POSTGRES_PASSWORD=$PG_PASS `
        -p "${PG_PORT}:5432" `
        --health-cmd "pg_isready -U $PG_USER -d $PG_DB" `
        --health-interval 2s `
        --health-timeout 3s `
        --health-retries 20 `
        $PG_IMAGE

    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Failed to start Docker container." -ForegroundColor Red
        exit 1
    }
}

# Wait until healthy
Write-Host "[INFO] Waiting for PostgreSQL to be ready ..."
$tries = 0
while ($true) {
    $health = docker inspect --format '{{.State.Health.Status}}' $CONTAINER 2>$null
    if ($health -eq 'healthy') {
        Write-Host "[INFO] PostgreSQL is healthy and accepting connections." -ForegroundColor Green
        break
    }
    $tries++
    if ($tries -gt 30) {
        Write-Host "[ERROR] PostgreSQL did not become healthy within 60s." -ForegroundColor Red
        exit 1
    }
    Write-Host "[INFO]   status=$health  ($tries/30) ..."
    Start-Sleep -Seconds 2
}

Write-Host "[INFO] Database: jdbc:postgresql://localhost:${PG_PORT}/${PG_DB}  user=$PG_USER"
exit 0
