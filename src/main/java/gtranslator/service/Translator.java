package gtranslator.service;

import gtranslator.domain.Language;
import gtranslator.domain.TranslateModel;
import gtranslator.service.impl.TranslatorImpl;

public interface Translator {

    interface Callback {
        void onComplete(TranslateModel model);

        void onFailure(Exception ex, String tag);
    }

    void translate(String source, Language srcLang, Language trgSrc, Callback callback);

    void handleTranslate(TranslatorImpl.TranslateEvent event);
}