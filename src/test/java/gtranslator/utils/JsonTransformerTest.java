package gtranslator.utils;

import gtranslator.domain.Language;
import gtranslator.domain.Phonetic;
import gtranslator.domain.TranslateModel;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.EnumSet;
import javax.json.JsonObject;
import org.junit.Test;

public class JsonTransformerTest {
    @Test
    public void test1() throws Exception {
        TranslateModel model = new TranslateModel();
        model.setText("test");
        model.setLang(Language.EN);
        model.addTranslation(Language.RU, "NOUN", "test1", BigDecimal.valueOf(1.0));
        model.addTranslation(Language.RU, "NOUN", "test2", BigDecimal.valueOf(0.5));
        model.addTranslation(Language.RU, "VERB", "test3", BigDecimal.valueOf(1.0));
        model.addTranscription(Phonetic.AM, "test1");
        model.addTranscription(Phonetic.AM, "test2");
        model.addTranscription(Phonetic.BR, "test3");
        JsonObject jsonObject = model.toJson(
                EnumSet.of(
                        TranslateModel.Fields.TRANSLATIONS,
                        TranslateModel.Fields.TRANSCRIPTIONS));
        JsonTransformer transformer = JsonTransformer.createJsonTransformer();
        transformer.setMaxCount(Integer.MAX_VALUE);
        String html = transformer.convertJsonToHtml(jsonObject.toString(), JsonTransformer.XSL.WORD);
        Path path = Paths.get("/tmp", "test.html");
        Files.copy(new ByteArrayInputStream(html.getBytes("utf-8")), path, StandardCopyOption.REPLACE_EXISTING);
    }
}
