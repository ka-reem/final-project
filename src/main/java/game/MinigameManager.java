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
        System.out.println("[DEBUG] Minigame completed: " + minigameClass.getSimpleName());
        System.out.println("[DEBUG] Current progress: " + toString());
    }

    public boolean isMinigameCompleted(Class<? extends Minigame> minigameClass) {
        boolean completed = completedMinigames.contains(minigameClass);
        System.out.println("[DEBUG] Checking completion for " + minigameClass.getSimpleName() + ": " + completed);
        return completed;
    }

    public void resetProgress() {
        System.out.println("[DEBUG] Resetting all minigame progress");
        completedMinigames.clear();
    }

    public int getCompletedCount() {
        System.out.println("[DEBUG] Total completed minigames: " + completedMinigames.size());
        return completedMinigames.size();
    }

    @Override
    public String toString() {
        return String.format("MinigameManager{completed=%d, games=%s}", 
            completedMinigames.size(), 
            completedMinigames.stream()
                .map(Class::getSimpleName)
                .toList());
    }
}