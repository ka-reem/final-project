
package game;

import java.util.HashSet;
import java.util.Set;

public class MinigameManager {
    private static MinigameManager instance;
    private Set<Class<? extends Minigame>> completedMinigames;

    private MinigameManager() {
        completedMinigames = new HashSet<>();
    }

    public static MinigameManager getInstance() {
        if (instance == null) {
            instance = new MinigameManager();
        }
        return instance;
    }

    public void markMinigameCompleted(Class<? extends Minigame> minigameClass) {
        completedMinigames.add(minigameClass);
    }

    public boolean isMinigameCompleted(Class<? extends Minigame> minigameClass) {
        return completedMinigames.contains(minigameClass);
    }

    public void resetProgress() {
        completedMinigames.clear();
    }

    public int getCompletedCount() {
        return completedMinigames.size();
    }
}