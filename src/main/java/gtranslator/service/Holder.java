package gtranslator.service;

import gtranslator.domain.Dictionary;
import gtranslator.domain.Language;
import gtranslator.domain.Phonetic;
import gtranslator.domain.Transcription;
import gtranslator.domain.Word;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface Holder {
    Dictionary loadDictionary(String source, Language srcLang, String target, Language trgLang);

    List<Dictionary> loadDictionary(String source, Language srcLang, Language trgLang);

    Word loadWord(String text, Language lang);

    Word saveWord(String text, Language lang, Set<Transcription> transcriptions);

    Dictionary saveDictionary(Word source, Word target, String category, BigDecimal weight);

    File getAudioIvonaFile(String engWord, Phonetic phonetic);

    File saveAudioIvonaFile(InputStream in, Phonetic phonetic, String fileName) throws IOException;

    File getAudioOxfordFile(String engWord, String transcription, Phonetic phonetic) throws UnsupportedEncodingException;

    File saveAudioOxfordFile(InputStream in, Phonetic phonetic, String word, String transcription) throws IOException;
}
