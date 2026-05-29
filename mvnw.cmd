@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE__=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $env:__MVNW_SCRIPT__='mvnw'; icm -ScriptBlock ([Scriptblock]::Create((Get-Content -Raw '%~f0'))) -NoNewScope}"`) DO @(
  IF "%%A"=="MVN_CMD" (set __MVNW_CMD__=%%B) ELSE IF "%%B"=="" (echo %%A) ELSE (echo %%A=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE__%
@SET __MVNW_PSMODULEP_SAVE__=
@SET __MVNW_ARG0_NAME__=
@SET __MVNW_CMD__=
@IF NOT "%__MVNW_ERROR__%"=="" @(
  @echo %__MVNW_ERROR__%
  @SET __MVNW_ERROR__=
  @exit /b 1
)

@REM Default Maven version
set DEFAULT_MAVEN_VERSION=3.9.6

@REM Find project base directory
set MAVEN_PROJECTBASEDIR=%~dp0
if not "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR%\

@REM Find Java
if defined JAVA_HOME goto findJavaFromJavaHome
set JAVACMD=java
goto execute

:findJavaFromJavaHome
set JAVACMD=%JAVA_HOME%\bin\java.exe

:execute
@REM Set Maven home
if defined MAVEN_HOME goto runMaven
if defined M2_HOME goto runMavenFromM2

@REM Download Maven if not present
set MAVEN_HOME=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\dists\apache-maven-%DEFAULT_MAVEN_VERSION%
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runMaven

echo Downloading Maven %DEFAULT_MAVEN_VERSION%...
set MAVEN_DOWNLOAD_URL=https://archive.apache.org/dist/maven/maven-3/%DEFAULT_MAVEN_VERSION%/binaries/apache-maven-%DEFAULT_MAVEN_VERSION%-bin.tar.gz

powershell -Command "& { New-Item -ItemType Directory -Force -Path '%MAVEN_HOME%' | Out-Null; $tempFile = [System.IO.Path]::GetTempFileName(); Invoke-WebRequest -Uri '%MAVEN_DOWNLOAD_URL%' -OutFile $tempFile; Add-Type -AssemblyName System.IO.Compression.FileSystem; [System.IO.Compression.ZipFile]::ExtractToDirectory($tempFile, '%MAVEN_PROJECTBASEDIR%.mvn\wrapper\dists\'); Remove-Item $tempFile; }"

if exist "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\dists\apache-maven-%DEFAULT_MAVEN_VERSION%-bin" (
  move "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\dists\apache-maven-%DEFAULT_MAVEN_VERSION%-bin" "%MAVEN_HOME%" >nul 2>&1
)

:runMavenFromM2
set MAVEN_HOME=%M2_HOME%

:runMaven
set MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd

@REM Execute Maven
"%JAVACMD%" ^
  %MAVEN_OPTS% ^
  %MAVEN_DEBUG_OPTS% ^
  -classpath "%MAVEN_HOME%\boot\plexus-classworlds-*.jar" ^
  "-Dclassworlds.conf=%MAVEN_HOME%\bin\m2.conf" ^
  "-Dmaven.home=%MAVEN_HOME%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  "%MVN_CMD%" %*

:end
