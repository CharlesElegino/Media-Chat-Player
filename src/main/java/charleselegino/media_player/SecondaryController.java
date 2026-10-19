package charleselegino.media_player;

import java.io.IOException;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.util.Duration;
import java.util.Collections;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

public class SecondaryController {

    private static final double SKIP_SECONDS = 10.0;
    private static final String PLAY_ICON = "\u25B6";
    private static final String PAUSE_ICON = "\u23F8";

    @FXML private BorderPane rootPane;
    @FXML private Label trackTitle;
    @FXML private Label trackArtist;
    @FXML private Label barTitle;
    @FXML private Label barArtist;
    @FXML private VBox emptyState;
    @FXML private StackPane artworkPanel;
    @FXML private ImageView wallpaperImage;
    @FXML private ScrollPane lyricsScroll;
    @FXML private VBox lyricsBox;
    @FXML private Button playButton;
    @FXML private javafx.scene.control.Slider seekSlider;
    @FXML private javafx.scene.control.Slider volumeSlider;
    @FXML private Label volumePercentLabel;
    @FXML private Label elapsedLabel;
    @FXML private Label durationLabel;
    private boolean useTimedLyrics;
    private List<lyricTest.Message> timedMessages = Collections.emptyList();
    private int renderedTimedMessageCount;

    @FXML
    private void initialize() {
        if (App.isDarkMode()) rootPane.getStyleClass().add("dark-theme");
        String title = App.getSelectedTitle();
        String artist = App.getSelectedArtist();
        trackTitle.setText(title);
        trackArtist.setText(artist);
        barTitle.setText(title);
        barArtist.setText(artist);
        useTimedLyrics = "Barbie Girl".equalsIgnoreCase(title.trim());
        if (useTimedLyrics) timedMessages = lyricTest.barbieGirl();
        String mediaUri = App.getSelectedMediaUri();
        volumePercentLabel.setText(formatVolumePercent(volumeSlider.getValue()));
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                volumePercentLabel.setText(formatVolumePercent(newValue.doubleValue())));
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            MediaPlayer activePlayer = App.getMediaPlayer();
            if (activePlayer != null) {
                activePlayer.setVolume(Math.max(0.0, Math.min(1.0, newValue.doubleValue())));
            }
        });
        initializePlayback(mediaUri);
        loadEmbeddedMetadata(mediaUri);

    }

    private void loadEmbeddedMetadata(String mediaUri) {
        if (mediaUri == null || mediaUri.isEmpty()) return;
        Thread metadataLoader = new Thread(() -> {
            TrackMetadata metadata = readMetadata(mediaUri);
            Platform.runLater(() -> displayMetadata(metadata));
        }, "track-metadata-reader");
        metadataLoader.setDaemon(true);
        metadataLoader.start();
    }

    private TrackMetadata readMetadata(String mediaUri) {
        Path temporaryFile = null;
        try {
            File audioFile;
            URI uri = URI.create(mediaUri);
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                audioFile = new File(uri);
            } else {
                URL url = new URL(mediaUri);
                String path = url.getPath();
                int extensionIndex = path.lastIndexOf('.');
                String extension = extensionIndex >= 0 && extensionIndex < path.length() - 1
                        ? path.substring(extensionIndex) : ".wav";
                temporaryFile = Files.createTempFile("media-player-tags-", extension);
                try (java.io.InputStream input = url.openStream()) {
                    Files.copy(input, temporaryFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                audioFile = temporaryFile.toFile();
            }
            AudioFile parsedFile = AudioFileIO.read(audioFile);
            Tag tag = parsedFile.getTag();
            if (tag == null) return new TrackMetadata(null, "");
            Artwork artwork = tag.getFirstArtwork();
            byte[] imageBytes = artwork == null ? null : artwork.getBinaryData();
            String lyrics = tag.getFirst(FieldKey.LYRICS);
            return new TrackMetadata(imageBytes, lyrics == null ? "" : lyrics);
        } catch (Exception exception) {
            return new TrackMetadata(null, "");
        } finally {
            if (temporaryFile != null) {
                try { Files.deleteIfExists(temporaryFile); } catch (IOException ignored) { }
            }
        }
    }

    private void displayMetadata(TrackMetadata metadata) {
        if (metadata.artwork != null && metadata.artwork.length > 0) {
            Image image = new Image(new ByteArrayInputStream(metadata.artwork));
            if (!image.isError()) {
                wallpaperImage.setImage(image);
                wallpaperImage.fitWidthProperty().bind(artworkPanel.widthProperty());
                wallpaperImage.fitHeightProperty().bind(artworkPanel.heightProperty());
            }
        }

        if (useTimedLyrics) {
            MediaPlayer player = App.getMediaPlayer();
            updateTimedLyrics(player == null ? 0 : player.getCurrentTime().toMillis());
            return;
        }

        lyricsBox.getChildren().clear();
        List<String> lines = new ArrayList<>();
        for (String line : metadata.lyrics.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) lines.add(trimmed);
        }
        for (int i = 0; i < lines.size(); i++) {
            Label bubble = new Label(lines.get(i));
            bubble.setWrapText(true);
            bubble.setMaxWidth(Math.max(240, artworkPanel.getWidth() * 0.68));
            bubble.getStyleClass().addAll("lyric-bubble", i % 2 == 0 ? "lyric-left" : "lyric-right");
            HBox row = new HBox(bubble);
            row.setAlignment(i % 2 == 0 ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
            lyricsBox.getChildren().add(row);
        }
        boolean hasLyrics = !lines.isEmpty();
        lyricsScroll.setVisible(hasLyrics);
        lyricsScroll.setManaged(hasLyrics);
        emptyState.setVisible(!hasLyrics);
        emptyState.setManaged(!hasLyrics);
        if (hasLyrics) scrollToLatestMessage();
    }

    private static final class TrackMetadata {
        private final byte[] artwork;
        private final String lyrics;

        private TrackMetadata(byte[] artwork, String lyrics) {
            this.artwork = artwork;
            this.lyrics = lyrics;
        }
    }

    private void initializePlayback(String mediaUri) {
        if (mediaUri == null || mediaUri.isEmpty()) {
            playButton.setDisable(true);
            return;
        }

        try {
            MediaPlayer existingPlayer = App.getMediaPlayer();
            if (existingPlayer != null && mediaUri.equals(App.getSelectedMediaUri())) {
                MediaPlayer player = existingPlayer;
                Duration duration = player.getTotalDuration();
                if (duration != null && !duration.isUnknown() && !duration.isIndefinite()) {
                    seekSlider.setMax(duration.toSeconds());
                    durationLabel.setText(formatTime(duration));
                }
                bindPlaybackControls(player);
                elapsedLabel.setText(formatTime(player.getCurrentTime()));
                seekSlider.setValue(player.getCurrentTime().toSeconds());
                playButton.setDisable(false);
                playButton.setText(player.getStatus() == MediaPlayer.Status.PLAYING ? PAUSE_ICON : PLAY_ICON);
                return;
            }

            MediaPlayer player = new MediaPlayer(new Media(mediaUri));
            App.setMediaPlayer(player);
            playButton.setDisable(false);
            player.setOnReady(() -> {
                Duration duration = player.getTotalDuration();
                seekSlider.setMax(duration.toSeconds());
                durationLabel.setText(formatTime(duration));
                player.play();
                playButton.setText(PAUSE_ICON);
            });
            bindPlaybackControls(player);
            player.setOnEndOfMedia(() -> playButton.setText(PLAY_ICON));
            player.setOnError(() -> {
                playButton.setDisable(true);
                playButton.setText("!");
            });
        } catch (RuntimeException exception) {
            playButton.setDisable(true);
            playButton.setText("!");
        }
    }

    private void bindPlaybackControls(MediaPlayer player) {
        volumeSlider.setValue(player.getVolume());
        player.currentTimeProperty().addListener((observable, oldTime, currentTime) -> {
            elapsedLabel.setText(formatTime(currentTime));
            if (!seekSlider.isValueChanging()) seekSlider.setValue(currentTime.toSeconds());
            if (useTimedLyrics) updateTimedLyrics(currentTime.toMillis());
        });
        seekSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> seekFromPointer(event, player));
        seekSlider.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> seekFromPointer(event, player));
    }

    @FXML
    private void addLyrics() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select timed lyrics");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("LRC lyrics", "*.lrc"));
        Window window = rootPane.getScene() == null ? null : rootPane.getScene().getWindow();
        File selectedFile = chooser.showOpenDialog(window);
        if (selectedFile == null) return;

        try {
            List<lyricTest.Message> selectedLyrics = parseLrc(selectedFile.toPath());
            if (selectedLyrics.isEmpty()) return;
            timedMessages = selectedLyrics;
            useTimedLyrics = true;
            lyricsBox.getChildren().clear();
            renderedTimedMessageCount = 0;
            lyricsScroll.setVisible(false);
            lyricsScroll.setManaged(false);
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            MediaPlayer player = App.getMediaPlayer();
            updateTimedLyrics(player == null ? 0 : player.getCurrentTime().toMillis());
        } catch (IOException exception) {
            // Keep the current lyric view when the selected file cannot be read.
        }
    }

    private List<lyricTest.Message> parseLrc(Path lrcFile) throws IOException {
        Pattern timestampPattern = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\]");
        List<lyricTest.Message> messages = new ArrayList<>();
        int lyricLineIndex = 0;
        for (String line : Files.readAllLines(lrcFile, java.nio.charset.StandardCharsets.UTF_8)) {
            Matcher matcher = timestampPattern.matcher(line);
            List<Long> timestamps = new ArrayList<>();
            while (matcher.find()) {
                long minutes = Long.parseLong(matcher.group(1));
                long seconds = Long.parseLong(matcher.group(2));
                String fraction = matcher.group(3);
                long fractionalMillis = 0;
                if (fraction != null) {
                    fractionalMillis = Long.parseLong(fraction);
                    if (fraction.length() == 1) fractionalMillis *= 100;
                    else if (fraction.length() == 2) fractionalMillis *= 10;
                }
                timestamps.add((minutes * 60 + seconds) * 1000 + fractionalMillis);
            }
            if (timestamps.isEmpty()) continue;
            String text = matcher.replaceAll("").trim();
            if (text.isEmpty()) continue;
            String speaker = lyricLineIndex++ % 2 == 0 ? "Dio" : "Jotaro";
            for (Long timestamp : timestamps) {
                messages.add(new lyricTest.Message(timestamp, speaker, text, null));
            }
        }
        messages.sort(Comparator.comparingLong(lyricTest.Message::getTimeMillis));
        return messages;
    }

    private void updateTimedLyrics(double timeMillis) {
        int visibleCount = 0;
        while (visibleCount < timedMessages.size()
                && timedMessages.get(visibleCount).getTimeMillis() <= timeMillis) {
            visibleCount++;
        }
        if (visibleCount == renderedTimedMessageCount) return;

        if (visibleCount < renderedTimedMessageCount) {
            lyricsBox.getChildren().clear();
            renderedTimedMessageCount = 0;
        }
        for (int i = renderedTimedMessageCount; i < visibleCount; i++) {
            lyricsBox.getChildren().add(createTimedBubble(timedMessages.get(i)));
        }
        renderedTimedMessageCount = visibleCount;
        boolean hasMessages = visibleCount > 0;
        lyricsScroll.setVisible(hasMessages);
        lyricsScroll.setManaged(hasMessages);
        emptyState.setVisible(!hasMessages);
        emptyState.setManaged(!hasMessages);
        if (hasMessages) {
            scrollToLatestMessage();
        }
    }

    private HBox createTimedBubble(lyricTest.Message message) {
        boolean fromJotaro = "Jotaro".equals(message.getSpeaker());
        Label timestamp = new Label(formatLyricTime(message.getTimeMillis()));
        timestamp.getStyleClass().add("lyric-timestamp");
        timestamp.setAlignment(fromJotaro ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(4);
        if (message.getImageResource() != null) {
            URL imageUrl = SecondaryController.class.getResource(message.getImageResource());
            if (imageUrl != null) {
                ImageView danceGif = new ImageView(new Image(imageUrl.toExternalForm()));
                danceGif.setFitWidth(240);
                danceGif.setFitHeight(180);
                danceGif.setPreserveRatio(true);
                danceGif.setSmooth(true);
                bubble.getChildren().add(danceGif);
            } else {
                Label missingImage = new Label("Dance GIF unavailable");
                missingImage.getStyleClass().add("lyric-message-text");
                bubble.getChildren().add(missingImage);
            }
        } else {
            Label text = new Label(message.getText());
            text.setWrapText(true);
            text.getStyleClass().add("lyric-message-text");
            bubble.getChildren().add(text);
        }
        bubble.getChildren().add(timestamp);
        bubble.setMaxWidth(Math.max(240, artworkPanel.getWidth() * 0.68));
        bubble.getStyleClass().addAll("lyric-bubble", fromJotaro ? "lyric-right" : "lyric-left");
        HBox row = new HBox(bubble);
        row.setAlignment(fromJotaro ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return row;
    }

    private void scrollToLatestMessage() {
        lyricsBox.requestLayout();
        lyricsScroll.requestLayout();
        Platform.runLater(() -> {
            lyricsBox.applyCss();
            lyricsBox.layout();
            lyricsScroll.applyCss();
            lyricsScroll.layout();
            lyricsScroll.setVvalue(lyricsScroll.getVmax());
            // A follow-up after the next layout pulse covers cases where the
            // scroll content's preferred height was recomputed asynchronously.
            Platform.runLater(() -> lyricsScroll.setVvalue(lyricsScroll.getVmax()));
        });
    }

    private void seekFromPointer(MouseEvent event, MediaPlayer player) {
        if (event.getButton() != MouseButton.PRIMARY && !event.isPrimaryButtonDown()) return;
        Node thumb = seekSlider.lookup(".thumb");
        Node target = event.getTarget() instanceof Node ? (Node) event.getTarget() : null;
        boolean pressedThumb = thumb != null && (target == thumb || isDescendantOf(target, thumb));
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED && !pressedThumb) event.consume();
        Bounds track = seekSlider.lookup(".track") == null
                ? null : seekSlider.lookup(".track").getBoundsInParent();
        Point2D point = seekSlider.sceneToLocal(event.getSceneX(), event.getSceneY());
        double start = track == null ? 8 : track.getMinX();
        double width = track == null ? seekSlider.getWidth() - 16 : track.getWidth();
        if (width <= 0 || seekSlider.getMax() <= seekSlider.getMin()) return;
        double ratio = Math.max(0, Math.min(1, (point.getX() - start) / width));
        double value = seekSlider.getMin() + ratio * (seekSlider.getMax() - seekSlider.getMin());
        seekSlider.setValue(value);
        player.seek(Duration.seconds(value));
    }

    private boolean isDescendantOf(Node node, Node ancestor) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current == ancestor) return true;
        }
        return false;
    }

    private String formatTime(Duration duration) {
        int seconds = Math.max(0, (int) duration.toSeconds());
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    private String formatVolumePercent(double volume) {
        return String.format("%d%%", Math.round(volume * 100));
    }

    private String formatLyricTime(long millis) {
        long seconds = millis / 1000;
        long remainder = millis % 1000;
        String timestamp = String.format("%d:%02d", seconds / 60, seconds % 60);
        return remainder == 0 ? timestamp : timestamp + "." + (remainder / 100);
    }

    @FXML
    private void togglePlay() {
        MediaPlayer player = App.getMediaPlayer();
        if (player == null) return;
        if (player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
            playButton.setText(PLAY_ICON);
        } else {
            player.play();
            playButton.setText(PAUSE_ICON);
        }
    }

    @FXML
    private void skipBackward() {
        skipBy(-SKIP_SECONDS);
    }

    @FXML
    private void skipForward() {
        skipBy(SKIP_SECONDS);
    }

    private void skipBy(double seconds) {
        MediaPlayer player = App.getMediaPlayer();
        if (player == null) return;

        Duration total = player.getTotalDuration();
        double max = total != null && !total.isUnknown() && !total.isIndefinite()
                ? total.toSeconds() : seekSlider.getMax();
        if (max <= 0 || Double.isNaN(max)) return;

        double current = player.getCurrentTime().toSeconds();
        double target = Math.max(0, Math.min(max, current + seconds));
        player.seek(Duration.seconds(target));
        seekSlider.setValue(target);
    }

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}
