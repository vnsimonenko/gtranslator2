package gtranslator.domain.converter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import gtranslator.domain.Phonetic;
import gtranslator.domain.Transcription;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

@Converter
public class JsonConverter extends TypeAdapter<Set<Transcription>> implements AttributeConverter<Set<Transcription>, String> {
    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .registerTypeAdapter(Set.class, this).create();

    @Override
    public String convertToDatabaseColumn(Set<Transcription> attribute) {
        return attribute == null || attribute.isEmpty() ? null : gson.toJson(attribute);
    }

    @Override
    public Set<Transcription> convertToEntityAttribute(String jText) {
        if (StringUtils.isBlank(jText)) {
            return Collections.emptySet();
        }
        return gson.fromJson(jText, Set.class);
    }


    @Override
    public void write(JsonWriter out, Set<Transcription> value) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Transcription> read(JsonReader in) throws IOException {
        Set<Transcription> transcriptions = new HashSet<>();
        in.beginArray();
        while (in.hasNext()) {
            in.beginObject();
            Transcription transcription = new Transcription();
            transcriptions.add(transcription);
            while (in.hasNext()) {
                String name = in.nextName();
                if ("text".equalsIgnoreCase(name)) {
                    transcription.setText(in.nextString());
                } else {
                    transcription.setPhonetic(Phonetic.valueOf(in.nextString()));
                }
            }
            in.endObject();
        }
        in.endArray();
        return transcriptions;
    }
}
