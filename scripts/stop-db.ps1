<#
.SYNOPSIS
    Stops and removes the cms-test-postgres Docker container.

.PARAMETER Keep
    Stop the container but do not remove it (preserves data volume).

.EXAMPLE
    .\stop-db.ps1
    .\stop-db.ps1 -Keep
#>
param(
    [switch]$Keep
)

$ErrorActionPreference = 'Continue'

$CONTAINER = 'cms-test-postgres'

$exists = docker inspect $CONTAINER 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "[INFO] Container '$CONTAINER' does not exist - nothing to stop." -ForegroundColor Yellow
    exit 0
}

Write-Host "[INFO] Stopping '$CONTAINER' ..."
docker stop $CONTAINER | Out-Null

if (-not $Keep) {
    Write-Host "[INFO] Removing '$CONTAINER' ..."
    docker rm $CONTAINER | Out-Null
    Write-Host "[INFO] Container removed." -ForegroundColor Green
} else {
    Write-Host "[INFO] Container stopped (kept for restart)." -ForegroundColor Green
}

exit 0
