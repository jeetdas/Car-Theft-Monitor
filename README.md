# Vigilante Cops

My project was to develop a dash cam application that will encourage people to help cops while being rewarded for their efforts and also help drive down their insurance prices. The application will be designed for Android phones so people can mount their phones while driving and they don’t have to worry anything beyond that. The app will take photos at regular intervals which would be analyzed to look for stolen vehicles and if any are found, the photos and the location of their capture would be notified to the police. Their driving can also be reported to to insurance companies for discounts.

## Prerequisities

Minimum SDK Version: 15
Target SDK Version:  26

## Features

- Recording (Enables the phone to take pictures every couple of seconds for analysis)
- Location Tracking (Reports the phone's co-ordinates to insurance companies for discounts)
- Rewards (Local companies can post rewards for enthusiasts here)

## How to run:

1. Open project in Android Studio (tested on version 3.0 preview).
2. Build and run application (tested on Android devices running version 25 or 26).
3. Click on start capturing to view results on the background.

## Built Using

- Java
- XML
- Android Studio 3.0 preview
- MongoDB (implemented using mLab)
- NodeJS
- HTML
- CSS
- JavaScript
- Heroku (for backend deployment)

## Acknowledgments
For number plate recognition and analysis, I used OpenALPR. It is an open-source project that allows analysis of images for number plate recognition.
http://www.openalpr.com/

