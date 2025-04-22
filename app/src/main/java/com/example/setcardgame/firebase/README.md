# Firebase Integration Guide

This file provides instructions for setting up Firebase with your Set Card Game app.

## 1. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" and follow the prompts to create a new project
3. Name it "SetCardGame" or similar

## 2. Add your Android app to Firebase

1. In your Firebase project, click the Android icon to add an Android app
2. Enter the package name: `com.example.setcardgame`
3. Enter a nickname (optional, e.g., "Set Card Game")
4. Register app

## 3. Download the google-services.json file

1. Download the `google-services.json` file provided by Firebase
2. Place it in your app directory (same level as your app's build.gradle file)

## 4. Generate a debug keystore and add SHA-1 fingerprint

1. Run the keystore generation tool provided in the `app/keystore` directory
2. After generating the keystore, get the SHA-1 fingerprint:
   ```
   keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
3. Copy the SHA-1 fingerprint
4. Go to your Firebase project settings
5. In the "Your apps" section, find your Android app
6. Click "Add fingerprint" and paste your SHA-1 fingerprint

## 5. Update the web client ID

1. In your Firebase project, go to "Authentication"
2. Click "Sign-in method" tab
3. Enable "Google" as a sign-in provider
4. Copy the "Web client ID" 
5. Replace the placeholder in `LoginActivity.java` with this Web client ID

## 6. Additional Firebase Setup

1. Go to "Realtime Database" in your Firebase Console
2. Click "Create Database"
3. Start in test mode for development
4. Create the necessary node structure:
   - leaderboard/
   - users/
   
## 7. Rules Setup for Realtime Database

Here are recommended security rules for your Firebase Realtime Database:

```json
{
  "rules": {
    "leaderboard": {
      ".read": true,
      ".write": "auth != null",
      "$scoreId": {
        ".validate": "newData.hasChildren(['playerName', 'score', 'timeInSeconds'])"
      }
    },
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

## Important Notes

1. Make sure to replace all placeholder values with real values from your Firebase project
2. The `google-services.json` file must be up to date and include your debug SHA-1 fingerprint
3. Don't commit sensitive Firebase credentials to public repositories
