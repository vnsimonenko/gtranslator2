package gtranslator.domain.validator;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import gtranslator.domain.validator.base.Validator;
import gtranslator.utils.JsonUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DictionaryValidator implements Validator<String> {
    @Override
    public boolean isValid(String dictionaryInJsonText) {
        if (StringUtils.isBlank(dictionaryInJsonText)) {
            return true;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("dictionary-scheme2.json")) {
            IOUtils.copy(in, out);
            String schemeText = new String(out.toByteArray(), "UTF-8");
            return JsonUtils.isJsonValid(schemeText, dictionaryInJsonText);
        } catch (IOException | ProcessingException ex) {
            //TODO RuntimeException
            throw new RuntimeException(ex);
        }
    }
}