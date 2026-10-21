$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$environmentRoot = Join-Path $projectRoot '_env'
$javaHome = Join-Path $environmentRoot 'Library\lib\jvm'
$binPath = Join-Path $environmentRoot 'Library\bin'
$mavenRepository = Join-Path $environmentRoot 'maven-repository'

if ((-not (Test-Path -LiteralPath (Join-Path $binPath 'mvn.cmd'))) -or
    (-not (Test-Path -LiteralPath (Join-Path $binPath 'java.exe')))) {
    throw "Conda environment is missing from $environmentRoot"
}

$env:JAVA_HOME = $javaHome
$env:PATH = "$binPath;$env:PATH"
$env:MAVEN_OPTS = "-Dmaven.repo.local=$($mavenRepository.Replace('\', '/'))"
$env:JAVA_TOOL_OPTIONS = "-Duser.home=$($environmentRoot.Replace('\', '/'))"

Write-Host "Using JDK and Maven from $environmentRoot"
Write-Host "Maven dependencies: $mavenRepository"
