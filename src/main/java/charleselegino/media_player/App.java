package charleselegino.media_player;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static String selectedTitle = "Midnight City";
    private static String selectedArtist = "M83";
    private static String selectedMediaUri;
    private static MediaPlayer mediaPlayer;
    private static boolean darkMode;

    static void setCurrentTrack(String title, String artist) {
        setCurrentTrack(title, artist, null);
    }

    static void setCurrentTrack(String title, String artist, String mediaUri) {
        selectedTitle = title;
        selectedArtist = artist;
        selectedMediaUri = mediaUri;
    }

    static String getSelectedTitle() { return selectedTitle; }
    static String getSelectedArtist() { return selectedArtist; }
    static String getSelectedMediaUri() { return selectedMediaUri; }
    static void setMediaPlayer(MediaPlayer player) {
        stopMediaPlayer();
        mediaPlayer = player;
    }
    static MediaPlayer getMediaPlayer() { return mediaPlayer; }
    static void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
    static boolean isDarkMode() { return darkMode; }
    static void setDarkMode(boolean enabled) { darkMode = enabled; }

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 1120, 720);
        scene.getStylesheets().add(App.class.getResource("application.css").toExternalForm());
        stage.setTitle("Media Chat Player");
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
