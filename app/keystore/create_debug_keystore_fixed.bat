@echo off
echo Creating debug keystore for Android application...
echo.

set KEYTOOL="C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"

:: Delete any existing keystore file to avoid format issues
if exist "debug.keystore" (
    echo Removing existing debug.keystore file...
    del "debug.keystore"
)

echo Generating new debug keystore...
echo.

%KEYTOOL% -genkeypair -v ^
    -storetype JKS ^
    -keystore "debug.keystore" ^
    -storepass "android" ^
    -alias "androiddebugkey" ^
    -keypass "android" ^
    -keyalg "RSA" ^
    -keysize 2048 ^
    -validity 10000 ^
    -dname "CN=Android Debug,O=Android,C=US"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Failed to create keystore.
    goto :end
)

echo.
echo Keystore created successfully!
echo.

:: Display the SHA-1 fingerprint
echo Getting SHA-1 fingerprint for Firebase setup...
echo.
%KEYTOOL% -list -v -keystore "debug.keystore" -alias "androiddebugkey" -storepass "android"

echo.
echo --------------------------------------------------------
echo IMPORTANT: Copy the SHA-1 fingerprint shown above to your
echo Firebase project settings to enable Firebase features.
echo --------------------------------------------------------

:end
echo.
pause
