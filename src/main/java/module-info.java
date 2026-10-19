module charleselegino.media_player {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jaudiotagger;

    opens charleselegino.media_player to javafx.fxml;
    exports charleselegino.media_player;
}
