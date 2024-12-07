
package game;

public class TopicManager {
    private static TopicManager instance;
    private String currentTopic;

    private TopicManager() {}

    public static TopicManager getInstance() {
        if (instance == null) {
            instance = new TopicManager();
        }
        return instance;
    }

    public void setTopic(String topic) {
        this.currentTopic = topic;
    }

    public String getTopic() {
        return currentTopic;
    }

    public boolean hasValidTopic() {
        return currentTopic != null && !currentTopic.trim().isEmpty();
    }
}