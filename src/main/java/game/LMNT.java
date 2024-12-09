package game;

import javax.sound.sampled.*;
import java.io.*;

public class LMNT {
    private static final String PYTHON_SCRIPT = "src/main/resources/lmnt_tts.py";
    private static final String OUTPUT_FILE = "output.wav";

    public static void synthesizeAndPlay(String text) {
        try {
            // Execute Python script
            ProcessBuilder pb = new ProcessBuilder("python3", PYTHON_SCRIPT, text);
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Play the generated audio file
                File audioFile = new File(OUTPUT_FILE);
                AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();

                // Wait for playback to complete
                while (!clip.isRunning())
                    Thread.sleep(10);
                while (clip.isRunning())
                    Thread.sleep(10);
                
                clip.close();
            } else {
                System.err.println("Error running Python script");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        synthesizeAndPlay("Hello, this is a test of LMNT text-to-speech.");
    }
}