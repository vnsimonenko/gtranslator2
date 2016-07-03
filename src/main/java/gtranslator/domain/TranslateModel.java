package gtranslator.domain;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import gtranslator.utils.Utils;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonObject;

/**
 * Structure {
 * transcriptions: [
 * phonetic: [
 * word
 * ]
 * ],
 * translations: [
 * language: [
 * category: [
 * word-weight
 * ]
 * ]
 * ]
 * }
 */
public class TranslateModel {
    @SerializedName("source")
    private volatile String text = "";
    @SerializedName("lang")
    private volatile Language lang = Language.EN;
    @SerializedName("transcriptions")
    private Map<Phonetic, Set<String>> transcriptions = Collections.synchronizedMap(new HashMap<>());
    @SerializedName("translations")
    private Map<Language, Map<String, Map<String, BigDecimal>>> translations = Collections.synchronizedMap(new HashMap<>());

    private String tag;

    public TranslateModel setText(String text) {
        this.text = text;
        return this;
    }

    public TranslateModel setLang(Language lang) {
        this.lang = lang;
        return this;
    }

    public String getText() {
        return text;
    }

    public Language getLang() {
        return lang;
    }

    public Map<Phonetic, Set<String>> getTranscriptions() {
        return transcriptions == null ? Collections.emptyMap() : transcriptions;
    }

    public Map<Language, Map<String, Map<String, BigDecimal>>> getTranslations() {
        return translations == null ? Collections.emptyMap() : translations;
    }

    public TranslateModel addTranslation(Language language, String category, String word, BigDecimal weight)
            throws InstantiationException, IllegalAccessException {
        synchronized (translations) {
            Map<String, Map<String, BigDecimal>> cats = Utils.getMultiplicityValueFromMap(translations, language, HashMap::new);
            Utils.getMultiplicityValueFromMap(cats, category, HashMap::new).put(word, weight);
        }
        return this;
    }

    public TranslateModel addTranscription(Phonetic phonetic, String transcription)
            throws InstantiationException, IllegalAccessException {
        synchronized (transcriptions) {
            Utils.getMultiplicityValueFromMap(transcriptions, phonetic, HashSet::new).add(transcription);
        }
        return this;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public enum Fields {
        TRANSCRIPTIONS("transcriptions"),
        TRANSLATIONS("translations"),
        SOURCE("source"),
        LANG("lang");

        Fields(String FIELD) {
            this.FIELD = FIELD;
        }

        public final String FIELD;
    }

    public JsonObject toJson(EnumSet<Fields> includeFields) {
        final Set<String> includes = includeFields == null || includeFields.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(Collections2.transform(includeFields, new Function<Fields, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Fields fields) {
                return fields.FIELD;
            }
        }));
        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return includes.size() > 0 && !includes.contains(fieldAttributes.getName());
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        }).setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        String jsonRepresentation = gson.toJson(this);
        return Json.createReader(new StringReader(jsonRepresentation)).readObject();
    }
}
