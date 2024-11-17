
package game;

public interface StreamingCallback {
    void onToken(String token);
    void onComplete();
    void onError(Throwable t);
}