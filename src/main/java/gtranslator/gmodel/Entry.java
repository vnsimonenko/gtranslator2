package gtranslator.gmodel;

import java.math.BigDecimal;

public class Entry {
    @com.google.api.client.util.Key("word")
    String word;
    @com.google.api.client.util.Key("score")
    BigDecimal score;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public BigDecimal getScore() {
        return score == null ? BigDecimal.ZERO : score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
}
