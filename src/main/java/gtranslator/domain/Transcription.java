package gtranslator.domain;

public class Transcription {
    private String text;
    private Phonetic phonetic;

    public Transcription() {
    }

    public Transcription(String text, Phonetic phonetic) {
        this.text = text;
        this.phonetic = phonetic;
    }

    public String getText() {
        return text;
    }

    public Phonetic getPhonetic() {
        return phonetic;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPhonetic(Phonetic phonetic) {
        this.phonetic = phonetic;
    }
}
