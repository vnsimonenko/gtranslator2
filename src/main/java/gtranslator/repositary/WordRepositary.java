package gtranslator.repositary;

import gtranslator.domain.Language;
import gtranslator.domain.Word;
import org.springframework.data.repository.CrudRepository;

public interface WordRepositary extends CrudRepository<Word, Long> {
    Word findByTextAndLang(String text, Language lang);

    Word save(Word word);
}
