package gtranslator.service.impl;


import gtranslator.domain.Dictionary;
import gtranslator.domain.Language;
import gtranslator.domain.Phonetic;
import gtranslator.domain.Transcription;
import gtranslator.domain.Word;
import gtranslator.repositary.DictionaryRepositary;
import gtranslator.repositary.WordRepositary;
import gtranslator.service.Holder;
import gtranslator.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HolderImpl implements Holder {

    @Autowired
    private DictionaryRepositary dictionaryRepositary;

    @Autowired
    private WordRepositary wordRepositary;

    @Value("${custom.prop.audioIvonaDir}")
    private String audioIvonaDir;

    @Value("${custom.prop.audioOxfordDir}")
    private String audioOxfordDir;

    @Override
    public Dictionary loadDictionary(String source, Language srcLang, String target, Language trgLang) {
        return dictionaryRepositary.find(source, srcLang, target, trgLang);
    }

    //@Cacheable(cacheNames = "dictionary2", key = "{#p0, #p1, #p2}")
    @Override
    public List<Dictionary> loadDictionary(String source, Language srcLang, Language trgLang) {
        return dictionaryRepositary.find(source, srcLang, trgLang);
    }

    public Word loadWord(String text, Language lang) {
        return wordRepositary.findByTextAndLang(text, lang);
    }

    @Transactional
    public Word saveWord(String text, Language lang, Set<Transcription> transcriptions) {
        Word word = wordRepositary.findByTextAndLang(text, lang);
        if (word == null) {
            word = new Word();
            word.setText(text);
            word.setLang(lang);
        }
        if (transcriptions != null && !transcriptions.isEmpty()) {
            word.setTranscriptions(transcriptions);
        }
        return wordRepositary.save(word);
    }

    @Transactional
    public Dictionary saveDictionary(Word source, Word target, String category, BigDecimal weight) {
        Dictionary dictionary = dictionaryRepositary.find(source.getText(), source.getLang(), target.getText(), target.getLang());
        Word src = wordRepositary.findOne(source.getId());
        Word trn = wordRepositary.findOne(target.getId());
        if (dictionary == null) {
            dictionary = new Dictionary();
        }
        dictionary.setSource(src);
        dictionary.setTarget(trn);
        dictionary.setWeight(weight);
        dictionary.setCategory(category);
        return dictionaryRepositary.save(dictionary);
    }

    public File getAudioIvonaFile(String engWord, Phonetic phonetic) {
        File dir = Paths.get(audioIvonaDir, phonetic.name().toLowerCase()).toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File f = Paths.get(dir.getPath(), engWord + ".mp3").toFile();
        return f.exists() ? f : null;
    }

    @Override
    public File saveAudioIvonaFile(InputStream in, Phonetic phonetic, String fileName) throws IOException {
        File dir = Paths.get(audioIvonaDir, phonetic.name().toLowerCase()).toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Path path = Paths.get(dir.getAbsolutePath(), fileName);
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        File f = Paths.get(dir.getPath(), fileName).toFile();
        return f.exists() ? f : null;
    }

    @Override
    public File getAudioOxfordFile(String engWord, String transcription, Phonetic phonetic) throws UnsupportedEncodingException {
        File dir = Paths.get(audioOxfordDir, phonetic.name().toLowerCase()).toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = Utils.encodeFileName(engWord + "_", transcription) + ".mp3";
        File f = Paths.get(dir.getPath(), fileName).toFile();
        return f.exists() ? f : null;
    }

    @Override
    public File saveAudioOxfordFile(InputStream in, Phonetic phonetic, String word, String transcription) throws IOException {
        File dir = Paths.get(audioOxfordDir, phonetic.name().toLowerCase()).toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = Utils.encodeFileName(word + "_", transcription) + ".mp3";
        Path path = Paths.get(dir.getAbsolutePath(), fileName);
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        File f = Paths.get(dir.getPath(), fileName).toFile();
        return f.exists() ? f : null;
    }
}
