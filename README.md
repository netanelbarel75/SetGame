# Set Card Game

An Android implementation of the classic Set card game with Google Sign-In and Firebase integration.

## Features

- Classic Set card game rules
- Single-player gameplay
- Google account integration
- Leaderboard functionality
- Game statistics
- Hints system

## Setup Instructions

1. Clone this repository
2. Open the project in Android Studio
3. Create a Firebase project at https://console.firebase.google.com/
4. Add your application to the Firebase project
5. Download the `google-services.json` file and replace the placeholder file in the app directory
6. Enable Google Sign-In in the Firebase Authentication section
7. Create a Firestore database with appropriate security rules
8. Build and run the application

## Game Rules

Set is a card game where the goal is to identify valid sets of three cards from the cards laid out on the table.

Each card has four features:
- Shape: oval, diamond, or squiggle
- Color: red, green, or purple
- Number: one, two, or three
- Shading: solid, striped, or outline

A valid set consists of three cards where each feature is either all the same or all different across the three cards.

Example: Three cards with all different shapes, all different colors, all different numbers, but all the same shading would be a valid set.

## Technologies Used

- Java
- Android SDK
- Firebase Authentication
- Firebase Firestore
- Google Sign-In API
- Android Fragments

## License

This project is licensed under the MIT License - see the LICENSE.md file for details.