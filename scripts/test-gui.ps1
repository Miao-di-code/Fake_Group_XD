param(
    [ValidateRange(1, 65535)] [int] $Port = 5555,
    [switch] $Admin
)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location -LiteralPath $projectRoot
& (Join-Path $PSScriptRoot 'Use-LocalConda.ps1')

& mvn -o -B install -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$testClass = if ($Admin) { 'GuiAdminIT' } else { 'GuiFlowIT' }
& mvn -o -B -pl client "-Dtest=$testClass" "-Dauction.gui.port=$Port" test
exit $LASTEXITCODE
