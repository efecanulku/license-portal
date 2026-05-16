# License Portal - local gelistirme sunucusu
# Kullanim: .\scripts\run-local.ps1  (repo kokunden)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$envFile = Join-Path $root ".env"
if (-not (Test-Path $envFile)) {
    Write-Error ".env bulunamadi. Once: copy env.example .env"
}

Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable($name, $value, 'Process')
    }
}

if (-not $env:SPRING_PROFILES_ACTIVE) {
    $env:SPRING_PROFILES_ACTIVE = 'local'
}

Write-Host "Profile: $env:SPRING_PROFILES_ACTIVE"
Write-Host "DB: $env:SPRING_DATASOURCE_URL"
Write-Host 'http://localhost:8080 - admin/password veya dealer/password'
Write-Host ''

& mvn spring-boot:run '-Dspring-boot.run.profiles=local'
