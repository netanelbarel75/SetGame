# Debug Keystore Creation

## Important: You need to create a debug keystore to complete Firebase integration

### Option 1: Create the keystore manually
1. Open a command prompt in this directory (app/keystore)
2. Run the following command:
```
keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
```
3. This will create the debug.keystore file in this directory

### Option 2: Use Android Studio
1. Open your project in Android Studio
2. Go to Tools → Firebase
3. Select Authentication or another Firebase service
4. Follow the setup wizard
5. Android Studio will handle keystore creation

### After creating the keystore, get the SHA-1 fingerprint
Run this command in the directory with the debug.keystore:
```
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Add this SHA-1 fingerprint to your Firebase project in the Firebase Console.
