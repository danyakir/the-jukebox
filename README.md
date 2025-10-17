# The JukeBox📻🎷🎉
![Platform](https://img.shields.io/badge/platform-Android-brightgreen.svg?color=00ADB5&style=for-the-badge)
## Preview📹

<img src="https://github.com/danyakir/the-jukebox/blob/main/SS/jukebox-video1.gif" width="300"/>
<img src="https://github.com/danyakir/the-jukebox/blob/main/SS/jukebox-video2.gif" width="300"/>

## Overview👀
This project was created for a festival I attended with my friends. We wanted to contribute something artistic and interactive to the event, so I came up with the idea of building a DIY jukebox that reacts to user input — changing both the music and the lights based on the selected genre.

The jukebox consisted of an Android tablet running a custom app, Bluetooth buttons, and LED lights ordered from AliExpress. When a user pressed one of the buttons, the app switched to a new genre, updated the UI to reflect the selection, changed the color of the LEDs, and played a random song from that genre.

## Concept🧠
- Goal: Create a digital art installation that combines sound, light, and visual design.
- Interaction: Six Bluetooth buttons, each representing a different music genre.
- Feedback: When a button is pressed, both the music and the LED lights change to match the selected genre.
- Display: A beautifully designed screen created by a digital painter friend, featuring custom visuals that complement the app’s mood and genre transitions.

## Implementation Details⛏
Platform: Android (Kotlin) 

Hardware: Bluetooth buttons and RGB LED strip (BLE-based)

Challenge:
Both the buttons and LEDs came without documentation, which made integration tricky.
To make them work, I had to reverse-engineer their Bluetooth protocols — experimenting with them and capturing their logs using tools like nRF Connect.


Features:
Detects Bluetooth button press events.
Maps each button to a music genre.
Updates the UI and LED colors dynamically.
Plays a random song from the selected genre (stored locally on the device).

## The Experience🎇
The final setup was displayed at the festival, and as expected it became a crowd favorite. People gathered around, pressed the buttons, switched genres, and danced as the lights and music changed. It was incredibly rewarding to see something that started as a simple DIY idea turn into a piece of shared art and energy.
