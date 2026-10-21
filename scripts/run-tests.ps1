param([switch] $Online)

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location -LiteralPath $projectRoot
& (Join-Path $PSScriptRoot 'Use-LocalConda.ps1')

if ($Online) {
    & mvn -B clean verify
} else {
    & mvn -o -B clean verify
}
exit $LASTEXITCODE
