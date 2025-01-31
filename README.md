## Version of Java Used:

OpenJDK 19.0.2

## IDE Used:

Visual Studio Code

## Steps to Import the Project into IDE:

1. Clone the repository.
2. Open the project in your preferred IDE.

## Steps to Build and Run Your Project:

1. Clone the repository.
2. Set the LMNT_API_KEY:

   - **Linux/MacOS:**

     ```bash
     export LMNT_API_KEY=redacte
     ```
   - **Windows:**

     ```cmd
     set LMNT_API_KEY=redacted
     ```
3. Run the following command:

#### For Windows:

```cmd
gradlew.bat clean build run
```

#### For macOS/Linux:

```bash
./gradlew clean build run
```

> **Note:** Was not able to get .jar working.

## Controls to play your game:

1. WASD keys.
2. The circles are other NPC's you can chat to. Start the game by navigating to them.
   3.The first message you send in chat will be your chosen topic to learn.
   4.To change the topic, press the "Back to Menu" button, then "Start Game" again.
   5.If you encounter issues answering questions(poorly generated question/answer), check the console output for the answers, they may be hidden sometimes (use Cmd+F for "answer"). You may also exit the minigame and return for a new question.
   6.Turning off the sound disables text-to-speech (won’t work mid-output) and also stops your player. You’ll need to toggle the chat window open/closed again.
   7.Important: You will need to close the chat after you send a message and want to move your character.

## Extra Notes

- **Linux/MacOS:**
  ```bash
  export LMNT_API_KEY=redacted
  ```

Note: Please do not share any keys or account IDs, whether that may be in this file or elsewhere.

Known Bugs & Solutions:
If your character is continuously moving in one direction (e.g., to the right), press the corresponding key again (e.g. right) to reset the movement.
The PDF reader button used to work but has been removed. It no longer functions due to rate-limiting when uploading large PDFs due to switching to a larger model. The button remains, but it’s curently turned off–I'd like to add it in the future if the rate-limits change.

If you'd like to change speech-to-text voice, comment out line 29 and uncomment line 28: /src/main/resources/lmnt_tts.py
