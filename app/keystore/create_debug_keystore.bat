@echo off
echo Creating debug keystore for Android application...
echo.

:: Set the keystore path and credentials
set KEYSTORE_PATH=debug.keystore
set KEYSTORE_PASS=android
set KEY_ALIAS=androiddebugkey
set KEY_PASS=android
set VALIDITY=10000

:: Display the path where the keystore will be created
echo Keystore will be created at: %~dp0%KEYSTORE_PATH%
echo.

:: Check if the keystore already exists
if exist "%KEYSTORE_PATH%" (
    echo WARNING: A keystore file already exists at this location.
    set /p OVERWRITE=Do you want to overwrite it? (Y/N): 
    if /i not "%OVERWRITE%"=="Y" (
        echo Keystore creation cancelled.
        goto :end
    )
    echo Overwriting existing keystore...
    del "%KEYSTORE_PATH%"
)

:: Create the keystore using keytool
echo Generating keystore with the following properties:
echo - Keystore file: %KEYSTORE_PATH%
echo - Keystore password: %KEYSTORE_PASS%
echo - Key alias: %KEY_ALIAS%
echo - Key password: %KEY_PASS%
echo - Validity: %VALIDITY% days
echo.

keytool -genkeypair -v ^
    -keystore "%KEYSTORE_PATH%" ^
    -storepass "%KEYSTORE_PASS%" ^
    -alias "%KEY_ALIAS%" ^
    -keypass "%KEY_PASS%" ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity %VALIDITY% ^
    -dname "CN=Android Debug,O=Android,C=US"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Failed to create keystore.
    echo Make sure that Java is installed and keytool is in your PATH.
    goto :end
)

echo.
echo Keystore created successfully!
echo.

:: Display the SHA-1 fingerprint
echo Getting SHA-1 fingerprint for Firebase setup...
echo.
keytool -list -v -keystore "%KEYSTORE_PATH%" -alias "%KEY_ALIAS%" -storepass "%KEYSTORE_PASS%"

echo.
echo --------------------------------------------------------
echo IMPORTANT: Copy the SHA-1 fingerprint shown above to your
echo Firebase project settings to enable Firebase features.
echo --------------------------------------------------------

:end
echo.
pause
