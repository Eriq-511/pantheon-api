<#
.SYNOPSIS
    Builds the backend Docker image using the multi-stage Dockerfile.

.PARAMETER Tag
    Docker image tag.  Defaults to 'pantheon-cms:latest'.

.PARAMETER NoCache
    Pass --no-cache to Docker (forces a full rebuild).

.EXAMPLE
    .\build.ps1
    .\build.ps1 -Tag "pantheon-cms:1.0.0"
    .\build.ps1 -NoCache
#>
param(
    [string]$Tag     = 'pantheon-cms:latest',
    [switch]$NoCache
)

$ErrorActionPreference = 'Continue'

$BACKEND_DIR = Split-Path $PSScriptRoot -Parent   # backend/

Write-Host "[INFO] Verifying Docker is available ..." -ForegroundColor Cyan
docker info 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Docker is not running. Start Docker Desktop first." -ForegroundColor Red
    exit 1
}

$cacheFlag = if ($NoCache) { '--no-cache' } else { '' }

Write-Host "[INFO] Building image '$Tag' from $BACKEND_DIR ..." -ForegroundColor Cyan
Set-Location $BACKEND_DIR
docker build $cacheFlag -t $Tag .

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[INFO] Image '$Tag' built successfully." -ForegroundColor Green
    Write-Host "[INFO] Run with:  docker run --rm -p 8080:8080 $Tag"
} else {
    Write-Host "`n[ERROR] Docker build failed." -ForegroundColor Red
}

exit $LASTEXITCODE
