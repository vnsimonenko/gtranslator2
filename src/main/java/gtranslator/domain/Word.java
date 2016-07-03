package gtranslator.domain;

import gtranslator.domain.converter.JsonConverter;
import gtranslator.domain.validator.SpellCheckingValidator;
import gtranslator.domain.validator.base.ValidatedBy;
import gtranslator.utils.Utils;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "word")
public class Word extends Identifier {
    @Column(name = "text", nullable = false)
    @ValidatedBy(SpellCheckingValidator.class)
    private String text;

    @Column(name = "lang", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Language lang;

    @Column(name = "transcriptions")
    @Convert(converter = JsonConverter.class)
    private Set<Transcription> transcriptions;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "source", cascade = {CascadeType.REFRESH})
    private Set<Dictionary> sources;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "target", cascade = {CascadeType.REFRESH})
    private Set<Dictionary> targets;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Language getLang() {
        return lang;
    }

    public void setLang(Language lang) {
        this.lang = lang;
    }

    public boolean isComposite() {
        return !Utils.isSingleWord(text);
    }

    public Set<Transcription> getTranscriptions() {
        if (transcriptions == null) {
            transcriptions = new HashSet<>();
        }
        return transcriptions;
    }

    public Set<Dictionary> getSources() {
        return sources;
    }

    public Set<Dictionary> getTargets() {
        return targets;
    }

    public void setTranscriptions(Set<Transcription> transcriptions) {
        this.transcriptions = transcriptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Word)) return false;

        Word word = (Word) o;

        if (getText() != null ? !getText().equals(word.getText()) : word.getText() != null) return false;
        return getLang() == word.getLang();

    }

    @Override
    public int hashCode() {
        int result = getText() != null ? getText().hashCode() : 0;
        result = 31 * result + (getLang() != null ? getLang().hashCode() : 0);
        return result;
    }
}
