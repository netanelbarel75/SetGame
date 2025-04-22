DEBUG KEYSTORE SETUP INSTRUCTIONS
===============================

This directory contains scripts to create a debug keystore for your Android application.

If you're on Windows:
1. Double-click the 'create_debug_keystore.bat' file
2. This will create a debug.keystore file in this directory

If you're on macOS or Linux:
1. Open a terminal in this directory
2. Run: chmod +x create_debug_keystore.sh
3. Run: ./create_debug_keystore.sh
4. This will create a debug.keystore file in this directory

After creating the keystore:
- You don't need to make any changes to your build.gradle file as it's already configured to use this keystore
- The debug.keystore will be used for signing debug builds automatically
- You'll need to get the SHA-1 or SHA-256 fingerprint to register with Firebase

To get the SHA-1 fingerprint on Windows:
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android

To get the SHA-1 fingerprint on macOS/Linux:
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android

Note: Remember to add this fingerprint to your Firebase project!
