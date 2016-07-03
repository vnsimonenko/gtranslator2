package gtranslator.service;

import gtranslator.domain.Phonetic;
import java.util.Map;
import java.util.Set;

public interface OxfordReceiver {
    Map<String, Map<Phonetic, Set<String>>> load(String enWord);
}
