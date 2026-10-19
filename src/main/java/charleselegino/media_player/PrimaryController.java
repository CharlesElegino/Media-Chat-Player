package charleselegino.media_player;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.RotateTransition;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

public class PrimaryController {

    private static final Set<String> knownSampleUris = ConcurrentHashMap.newKeySet();
    private static final java.util.Map<String, String> observedSampleVersions = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService sampleFolderScanner = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "sample-music-folder-scanner");
        thread.setDaemon(true);
        return thread;
    });
    private static boolean sampleFolderScannerStarted;
    private static final ObservableList<Track> tracks = createDefaultTracks();

    private static ObservableList<Track> createDefaultTracks() {
        List<SampleAudio> samples = discoverSampleAudio();
        ObservableList<Track> discovered = FXCollections.observableArrayList();
        for (int i = 0; i < samples.size(); i++) {
            SampleAudio sample = samples.get(i);
            discovered.add(createTrack(sample, i));
            knownSampleUris.add(sample.uri);
        }
        return discovered;
    }

    private static Track createTrack(SampleAudio sample, int index) {
        String basename = sample.name.substring(0, sample.name.lastIndexOf('.'));
        String title = basename;
        String artist = "Local audio file";
        if (basename.equalsIgnoreCase("Barbie Girl - Aqua")) {
            title = "Barbie Girl";
            artist = "Aqua";
        } else if (basename.equalsIgnoreCase("Idol - YOASOBI REAL")) {
            title = "Idol";
            artist = "YOASOBI";
        } else {
            int separator = basename.indexOf(" - ");
            if (separator > 0 && separator + 3 < basename.length()) {
                artist = basename.substring(0, separator).trim();
                title = basename.substring(separator + 3).trim();
            }
        }
        String[] colors = {"#7566B8", "#E89B72", "#568B9E", "#71966B", "#BA7187"};
        String glyph = title.isEmpty() ? "♫" : title.substring(0, 1).toUpperCase();
        return new Track(title, artist, "—", colors[index % colors.length], glyph, sample.uri);
    }

    private static List<SampleAudio> discoverSampleAudio() {
        List<SampleAudio> samples = new ArrayList<>();
        Path sourceFolder = Paths.get("src/main/java/charleselegino/media_player/music sample");
        try {
            if (Files.isDirectory(sourceFolder)) {
                try (java.util.stream.Stream<Path> files = Files.list(sourceFolder)) {
                    files.filter(Files::isRegularFile)
                            .filter(path -> isSupportedAudio(path.getFileName().toString()))
                            .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                            .forEach(path -> samples.add(new SampleAudio(path.getFileName().toString(), path.toUri().toString())));
                }
                return samples;
            }

            URL folderUrl = PrimaryController.class.getResource("music sample/");
            if (folderUrl == null) return samples;
            if ("file".equalsIgnoreCase(folderUrl.getProtocol())) {
                try (java.util.stream.Stream<Path> files = Files.list(Paths.get(folderUrl.toURI()))) {
                    files.filter(Files::isRegularFile)
                            .filter(path -> isSupportedAudio(path.getFileName().toString()))
                            .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                            .forEach(path -> {
                                URL resource = PrimaryController.class.getResource("music sample/" + path.getFileName());
                                if (resource != null) samples.add(new SampleAudio(path.getFileName().toString(), resource.toExternalForm()));
                            });
                }
            } else if ("jar".equalsIgnoreCase(folderUrl.getProtocol())) {
                JarURLConnection connection = (JarURLConnection) folderUrl.openConnection();
                String prefix = connection.getEntryName();
                if (prefix == null) prefix = "charleselegino/media_player/music sample/";
                try (JarFile jar = connection.getJarFile()) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (entry.isDirectory() || !entryName.startsWith(prefix)) continue;
                        String name = entryName.substring(prefix.length());
                        if (name.contains("/") || !isSupportedAudio(name)) continue;
                        URL resource = PrimaryController.class.getResource("music sample/" + name);
                        if (resource != null) samples.add(new SampleAudio(name, resource.toExternalForm()));
                    }
                }
                samples.sort(Comparator.comparing(sample -> sample.name.toLowerCase()));
            }
        } catch (Exception exception) {
            // Keep the library usable even when the bundled sample folder cannot be scanned.
        }
        return samples;
    }

    private static boolean isSupportedAudio(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a")
                || lower.endsWith(".aif") || lower.endsWith(".aiff");
    }

    private static final class SampleAudio {
        private final String name;
        private final String uri;

        private SampleAudio(String name, String uri) {
            this.name = name;
            this.uri = uri;
        }
    }

    private static synchronized void startSampleFolderScanner() {
        if (sampleFolderScannerStarted) return;
        Path sampleFolder = Paths.get("src/main/java/charleselegino/media_player/music sample").toAbsolutePath().normalize();
        if (!Files.isDirectory(sampleFolder)) return;
        sampleFolderScannerStarted = true;
        sampleFolderScanner.scheduleWithFixedDelay(() -> scanForAddedMusic(sampleFolder), 2, 2, TimeUnit.SECONDS);
    }

    private static void scanForAddedMusic(Path sampleFolder) {
        try (java.util.stream.Stream<Path> files = Files.list(sampleFolder)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> isSupportedAudio(path.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            String uri = path.toUri().toString();
                            if (knownSampleUris.contains(uri)) return;
                            long size = Files.size(path);
                            if (size == 0) return;
                            String version = size + ":" + Files.getLastModifiedTime(path).toMillis();
                            String previousVersion = observedSampleVersions.put(uri, version);
                            if (!version.equals(previousVersion)) return;
                            if (!knownSampleUris.add(uri)) return;

                            SampleAudio sample = new SampleAudio(path.getFileName().toString(), uri);
                            Platform.runLater(() -> {
                                boolean alreadyPresent = tracks.stream().anyMatch(track -> track.mediaUri.equals(uri));
                                if (!alreadyPresent) tracks.add(createTrack(sample, tracks.size()));
                            });
                        } catch (IOException ignored) {
                            // A file may still be copying; a later scan will try again.
                        }
                    });
        } catch (IOException ignored) {
            // Retry on the next scan if the folder is temporarily unavailable.
        }
    }

    @FXML private ListView<Track> trackList;
    @FXML private TextField searchField;
    @FXML private BorderPane rootPane;
    @FXML private Button themeButton;
    @FXML private Label themeOrb;
    @FXML private Label themeStars;
    @FXML private Label themeClouds;
    @FXML private StackPane themeTrack;
    @FXML
    private void initialize() {
        applyTheme();
        startSampleFolderScanner();
        FilteredList<Track> filteredTracks = new FilteredList<>(tracks, track -> true);
        trackList.setItems(filteredTracks);
        trackList.setCellFactory(list -> new TrackCell());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase();
            filteredTracks.setPredicate(track -> query.isEmpty()
                    || track.title.toLowerCase().contains(query)
                    || track.artist.toLowerCase().contains(query));
        });
        trackList.getSelectionModel().selectedItemProperty().addListener((observable, oldTrack, selectedTrack) -> {
            if (selectedTrack != null) {
                openTrack(selectedTrack);
            }
        });
    }

    @FXML
    private void addMusic() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select music");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Playable audio files", "*.mp3", "*.wav", "*.m4a", "*.aif", "*.aiff"));
        Window window = rootPane.getScene() == null ? null : rootPane.getScene().getWindow();
        File selectedFile = chooser.showOpenDialog(window);
        if (selectedFile == null) return;

        String filename = selectedFile.getName();
        int extension = filename.lastIndexOf('.');
        String title = extension > 0 ? filename.substring(0, extension) : filename;
        Track addedTrack = new Track(title, "Local music file", "—", "#7566B8", "♫", selectedFile.toURI().toString());
        tracks.add(0, addedTrack);
        openTrack(addedTrack);
    }

    @FXML
    private void toggleTheme() {
        App.setDarkMode(!App.isDarkMode());
        applyTheme();
    }

    private void applyTheme() {
        rootPane.getStyleClass().remove("dark-theme");
        themeTrack.getStyleClass().remove("night");
        if (App.isDarkMode()) {
            rootPane.getStyleClass().add("dark-theme");
            themeOrb.getStyleClass().add("night");
            themeTrack.getStyleClass().add("night");
            themeOrb.setText("☾");
            themeStars.setOpacity(1.0);
            themeClouds.setOpacity(0.0);
            themeButton.setAccessibleText("Dark mode on. Activate to switch to light mode.");
        } else {
            themeOrb.getStyleClass().remove("night");
            themeOrb.setText("☀");
            themeStars.setOpacity(0.0);
            themeClouds.setOpacity(1.0);
            themeButton.setAccessibleText("Dark mode off. Activate to switch to dark mode.");
        }
        RotateTransition spin = new RotateTransition(Duration.millis(250), themeOrb);
        spin.setByAngle(180);
        spin.play();
    }

    @FXML
    private void openSelectedTrack() throws IOException {
        Track selected = trackList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        openTrack(selected);
    }

    private void openTrack(Track selected) {
        App.stopMediaPlayer();
        App.setCurrentTrack(selected.title, selected.artist, selected.mediaUri);
        try {
            App.setRoot("secondary");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not open the player view", exception);
        }
    }

    private static final class Track {
        private final String title;
        private final String artist;
        private final String duration;
        private final String color;
        private final String glyph;
        private final String mediaUri;

        private Track(String title, String artist, String duration, String color, String glyph, String mediaUri) {
            this.title = title;
            this.artist = artist;
            this.duration = duration;
            this.color = color;
            this.glyph = glyph;
            this.mediaUri = mediaUri;
        }
    }

    private static final class TrackCell extends ListCell<Track> {
        @Override
        protected void updateItem(Track track, boolean empty) {
            super.updateItem(track, empty);
            if (empty || track == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            Label album = new Label(track.glyph);
            album.setAlignment(Pos.CENTER);
            album.setMinSize(42, 42);
            album.setStyle("-fx-background-color: " + track.color + "; -fx-background-radius: 12; -fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;");
            Label title = new Label(track.title);
            title.getStyleClass().add("track-title");
            Label artist = new Label(track.artist);
            artist.getStyleClass().add("track-artist");
            VBox text = new VBox(3, title, artist);
            text.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(text, Priority.ALWAYS);
            Label duration = new Label(track.duration);
            duration.getStyleClass().add("track-duration");
            HBox row = new HBox(12, album, text, duration);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("track-row");
            setGraphic(row);
        }
    }
}
