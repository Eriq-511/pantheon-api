@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------
@SETLOCAL
@SET "ERROR_CODE=0"
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0") ELSE (SET "BASE_DIR=%__MVNW_ARG0_NAME__%")
@SET MAVEN_PROJECTBASEDIR=%BASE_DIR%
@IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"
@IF "%MAVEN_PROJECTBASEDIR%" == "" GOTO error
@IF NOT "%MAVEN_BASEDIR%" == "" SET "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"

@SET WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper
@SET WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar"

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%WRAPPER_DIR%\maven-wrapper.properties") DO (
    @IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

@IF EXIST "%WRAPPER_JAR%" (
    @IF "%MVNW_VERBOSE%" == "true" (
        @echo Found %WRAPPER_JAR%
    )
) ELSE (
    @echo Downloading from: %DOWNLOAD_URL%
    @powershell -Command "&{"^
        "$webclient = new-object System.Net.WebClient;"^
        "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
        "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
        "}"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')"^
        "}"
    @IF "%ERRORLEVEL%"=="0" (@echo Downloaded successfully) ELSE (
        @echo Copying from Maven local repository...
        @copy "%M2_HOME%\.m2\repository\org\apache\maven\wrapper\maven-wrapper\3.3.1\maven-wrapper-3.3.1.jar" "%WRAPPER_JAR%" >NUL 2>&1
        @IF "%ERRORLEVEL%"=="0" (@echo) ELSE (
            @echo Failed to download maven-wrapper.jar, please download it manually.
            @goto error
        )
    )
)

@SET "MAVEN_JAVA_EXE=%JAVA_HOME%\bin\java.exe"
@IF NOT "%JAVA_HOME%" == "" GOTO init
@FOR %%i IN (java.exe) DO @SET "MAVEN_JAVA_EXE=%%~$PATH:i"

:init
@IF "%MAVEN_JAVA_EXE%"=="" (
    @echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
    @goto error
)

@SET CLASSWORLDS_CONF=%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config
@PUSHD "%MAVEN_PROJECTBASEDIR%" || GOTO error
@"%MAVEN_JAVA_EXE%" %JVM_CONFIG_MAVEN_PROPS% %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
@SET "ERROR_CODE=%ERRORLEVEL%"
@POPD
@IF NOT "%ERROR_CODE%"=="0" GOTO error
@GOTO end

:error
@IF "%ERROR_CODE%"=="0" @SET ERROR_CODE=1

:end
@ENDLOCAL & SET ERROR_CODE=%ERROR_CODE%
@IF NOT "%SUREFIRE_TIMEOUT%" == "" EXIT /B %ERROR_CODE%
@EXIT /B %ERROR_CODE%
