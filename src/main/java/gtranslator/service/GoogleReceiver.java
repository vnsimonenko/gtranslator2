package gtranslator.service;

import gtranslator.domain.Language;
import gtranslator.gmodel.Dics;

public interface GoogleReceiver {
    Dics translate(String text, Language from, Language to) throws Exception;
}
