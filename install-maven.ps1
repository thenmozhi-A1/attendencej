$mavenVersion = "3.9.6"
$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$destDir = Join-Path $baseDir ".mvn\wrapper\dists\apache-maven-$mavenVersion"
$mvnCmd = Join-Path $destDir "apache-maven-$mavenVersion\bin\mvn.cmd"

if (Test-Path $mvnCmd) {
    Write-Host "Maven already installed at $mvnCmd"
    exit 0
}

Write-Host "Creating destination: $destDir"
New-Item -ItemType Directory -Force -Path $destDir | Out-Null

$tempFile = Join-Path $env:TEMP "maven-$mavenVersion.zip"
$url = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"

Write-Host "Downloading Maven $mavenVersion from $url ..."
Invoke-WebRequest -Uri $url -OutFile $tempFile

Write-Host "Extracting to $destDir ..."
Expand-Archive -Path $tempFile -DestinationPath $destDir -Force

Remove-Item $tempFile
Write-Host "Maven installed successfully!"
Get-ChildItem (Join-Path $destDir "apache-maven-$mavenVersion\bin")
