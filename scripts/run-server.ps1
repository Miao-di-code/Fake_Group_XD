param(
    [ValidateRange(1, 65535)] [int] $Port = 5555,
    [string] $DatabasePath = 'data/auction.db',
    [ValidateRange(0, 3600)] [int] $AntiSnipeWindowSeconds = 30,
    [ValidateRange(0, 3600)] [int] $AntiSnipeExtensionSeconds = 30
)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location -LiteralPath $projectRoot
& (Join-Path $PSScriptRoot 'Use-LocalConda.ps1')

& mvn -o -B install -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& mvn -o -B -pl server exec:java `
    '-Dexec.mainClass=com.btl.auction.server.ServerMain' `
    "-Dauction.snipe.window.seconds=$AntiSnipeWindowSeconds" `
    "-Dauction.snipe.extension.seconds=$AntiSnipeExtensionSeconds" `
    "-Dexec.args=$Port $DatabasePath"
exit $LASTEXITCODE
