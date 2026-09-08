# Iniciar el Tomcat local preparado en .runtime, con MySQL ya disponible en 3307.
# Configurar DPAO_APP_USER y DPAO_APP_PASSWORD antes de ejecutar.
$ErrorActionPreference='Stop'
$projectPath=Split-Path -Parent $PSScriptRoot
$tomcatPath=Join-Path $projectPath '.runtime/tomcat/apache-tomcat-9.0.115'
$jdkPath=(Get-ChildItem (Join-Path $projectPath '.runtime/jdk17') -Directory | Select-Object -First 1).FullName
if (!$env:DPAO_APP_USER -or !$env:DPAO_APP_PASSWORD) { throw 'Configura la cuenta de acceso en las variables DPAO_APP_USER y DPAO_APP_PASSWORD.' }
if (!$env:DPAO_DB_USER) { throw 'Configura DPAO_DB_USER y DPAO_DB_PASSWORD para el MySQL local.' }
$env:DPAO_DB_URL='jdbc:mysql://127.0.0.1:3307/dpao_demo?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8'
$argsJava=@("-Dcatalina.home=$tomcatPath","-Dcatalina.base=$tomcatPath",'-Dfile.encoding=UTF-8','-cp',"$tomcatPath/bin/bootstrap.jar;$tomcatPath/bin/tomcat-juli.jar",'org.apache.catalina.startup.Bootstrap','start')
Start-Process -FilePath "$jdkPath/bin/java.exe" -ArgumentList $argsJava -WindowStyle Hidden -RedirectStandardOutput (Join-Path $projectPath '.runtime/demo-out.log') -RedirectStandardError (Join-Path $projectPath '.runtime/demo-error.log')
Write-Output 'Demostración: http://localhost:8080/dpao-cookies-web/acceso'
