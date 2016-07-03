package gtranslator.domain;

public enum Language {
    RU("ru"), EN("en"), UA("uk");

    Language(String google) {
        this.GOOGLE = google;
    }

    public final String GOOGLE;
}
