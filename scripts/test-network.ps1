$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location -LiteralPath $projectRoot
& (Join-Path $PSScriptRoot 'Use-LocalConda.ps1')

& mvn -o -B clean verify
exit $LASTEXITCODE
