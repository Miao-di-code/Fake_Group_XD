param(
    [ValidateRange(1, 65535)] [int] $Port = 5555,
    [string] $DatabasePath = 'data/demo.db'
)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location -LiteralPath $projectRoot
& (Join-Path $PSScriptRoot 'seed-demo.ps1') -DatabasePath $DatabasePath
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host 'Demo accounts: demo_admin, demo_seller, demo_bidder_a, demo_bidder_b'
Write-Host 'Local demo password: DemoPass123! (or AUCTION_DEMO_PASSWORD if set)'
Write-Host 'Open another PowerShell and run .\scripts\run-client.ps1 for each client.'
& (Join-Path $PSScriptRoot 'run-server.ps1') -Port $Port -DatabasePath $DatabasePath
exit $LASTEXITCODE
