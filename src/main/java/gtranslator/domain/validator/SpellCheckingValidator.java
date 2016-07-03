package gtranslator.domain.validator;

import gtranslator.domain.validator.base.Validator;
import org.springframework.stereotype.Component;

@Component
public class SpellCheckingValidator implements Validator<String> {
    @Override
    public boolean isValid(String text) {
        return text.matches(".*[a-zA-Zа-яА-ЯїіЇІ]+.*");
    }
}