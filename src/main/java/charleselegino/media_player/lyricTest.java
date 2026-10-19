package charleselegino.media_player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Timed sample chat for the bundled Barbie Girl track. */
public final class lyricTest {
    private lyricTest() { }

    public static List<Message> barbieGirl() {
        List<Message> messages = new ArrayList<>();
        addImage(messages, 500, "Dio", "/charleselegino/media_player/assets/diodance.gif");
        addImage(messages, 1500, "Jotaro", "/charleselegino/media_player/assets/jotarodance.gif");
        add(messages, 2500, "Jotaro", "Hiya, Dio");
        add(messages, 3500, "Dio", "Hi, Jotaro <3");
        add(messages, 4500, "Jotaro", "You want to go for a ride?");
        add(messages, 6000, "Dio", "Sure, Jotaro");
        add(messages, 7000, "Jotaro", "Jump in");
        add(messages, 8500, "Dio", "I'm Dio Brando, in the JoJo world!");
        add(messages, 12500, "Dio", "Life as vampire, it's fantastic");
        add(messages, 15500, "Dio", "You can brush my hair, undress me everywhere");
        add(messages, 19000, "Dio", "Imagination, life is your creation");
        add(messages, 23000, "Jotaro", "Come on, Barbie, let's go party");
        add(messages, 25000, "Dio", "I'm Dio Brando, in the JoJo world");
        add(messages, 29000, "Dio", "Life as vampire, it's fantastic");
        add(messages, 32500, "Dio", "You can brush my hair, undress me everywhere");
        add(messages, 36000, "Dio", "Imagination, life is your creation");
        add(messages, 41000, "Dio", "I'm a blonde bimbo girl in a fantasy world");
        add(messages, 44500, "Dio", "Dress me up, make it tight, I'm your dolly");
        add(messages, 48000, "Jotaro", "You're my doll, rock'n'roll, feel the glamour in pink");
        add(messages, 51500, "Jotaro", "Kiss me here, touch me there, hanky panky");
        add(messages, 55500, "Dio", "You can touch, You can play");
        add(messages, 59000, "Dio", "If you say, I'm always yours");
        add(messages, 63000, "Dio", "I'm Dio Brando, in the JoJo world");
        add(messages, 67500, "Dio", "Life as vampire, it's fantastic");
        add(messages, 71000, "Dio", "You can brush my hair, undress me everywhere");
        add(messages, 75000, "Dio", "Imagination, life is your creation");
        add(messages, 78000, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 80000, "Dio", "ah ah ah yeah");
        add(messages, 81500, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 84000, "Dio", "ooh oh, ooh oh");
        add(messages, 85500, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 87500, "Dio", "ah ah ah yeah");
        add(messages, 89000, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 91500, "Dio", "ooh oh, ooh oh");
        add(messages, 94000, "Dio", "Make me walk, make me talk, do whatever you please");
        add(messages, 98000, "Dio", "I can act like a star, I can beg on my knees");
        add(messages, 101500, "Jotaro", "Come jump in, bimbo friend, let us do it again");
        add(messages, 105000, "Jotaro", "Hit the town, fool around, let's go party");
        add(messages, 109000, "Dio", "You can touch, You can play");
        add(messages, 112500, "Dio", "If you say,");
        add(messages, 114500, "Dio", "I'm always yours");
        add(messages, 116500, "Dio", "You can touch, You can play");
        add(messages, 120000, "Dio", "If you say,");
        add(messages, 121000, "Dio", "I'm always yours");
        add(messages, 124000, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 126000, "Dio", "ah ah ah yeah");
        add(messages, 128000, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 130000, "Dio", "ooh oh, ooh oh");
        add(messages, 132500, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 134000, "Dio", "ah ah ah yeah");
        add(messages, 136000, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 137000, "Dio", "ooh oh, ooh oh");
        add(messages, 139000, "Dio", "I'm Dio Brando, in the JoJo world!");
        add(messages, 143000, "Dio", "Life as vampire, it's fantastic");
        add(messages, 146500, "Dio", "You can brush my hair, undress me everywhere");
        add(messages, 150500, "Dio", "Imagination, life is your creation");
        add(messages, 154000, "Dio", "I'm Dio Brando, in the JoJo world!");
        add(messages, 158000, "Dio", "Life as vampire, it's fantastic");
        add(messages, 161500, "Dio", "You can brush my hair, undress me everywhere");
        add(messages, 165500, "Dio", "Imagination, life is your creation");
        add(messages, 169000, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 170500, "Dio", "ah ah ah yeah");
        add(messages, 172000, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 174500, "Dio", "ooh oh, ooh oh");
        add(messages, 176000, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 177500, "Dio", "ah ah ah yeah");
        add(messages, 179500, "Jotaro", "Come on, Dio, let's go party");
        add(messages, 181500, "Dio", "ooh oh, ooh oh");
        add(messages, 184000, "Dio", "Oh, I'm having so much fun");
        add(messages, 186500, "Jotaro", "Well, Dio, we're just getting started");
        add(messages, 189000, "Dio", "Oh, I love you, Jotaro <3");
        add(messages, 191500, "Jotaro", "[M U S I C]");
        add(messages, 192500, "Dio", "/charleselegino/media_player/assets/mudamuda.gif");
        add(messages, 192500, "Dio", "[E N D]");
        messages.sort(Comparator.comparingLong(Message::getTimeMillis));
        return messages;
    }

    private static void add(List<Message> messages, long timeMillis, String speaker, String text) {
        String lower = text == null ? "" : text.toLowerCase();
        if (lower.endsWith(".gif") || lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            addImage(messages, timeMillis, speaker, text);
        } else {
            messages.add(new Message(timeMillis, speaker, text, null));
        }
    }

    private static void addImage(List<Message> messages, long timeMillis, String speaker, String imageResource) {
        messages.add(new Message(timeMillis, speaker, "", imageResource));
    }

    public static final class Message {
        private final long timeMillis;
        private final String speaker;
        private final String text;
        private final String imageResource;

        public Message(long timeMillis, String speaker, String text, String imageResource) {
            this.timeMillis = timeMillis;
            this.speaker = speaker;
            this.text = text;
            this.imageResource = imageResource;
        }

        public long getTimeMillis() { return timeMillis; }
        public String getSpeaker() { return speaker; }
        public String getText() { return text; }
        public String getImageResource() { return imageResource; }
    }
}
