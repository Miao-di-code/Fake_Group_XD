$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location -LiteralPath $projectRoot
& (Join-Path $PSScriptRoot 'Use-LocalConda.ps1')

& mvn -o -B install -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& mvn -o -B -pl client javafx:run
exit $LASTEXITCODE
