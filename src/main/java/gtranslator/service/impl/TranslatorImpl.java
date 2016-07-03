package gtranslator.service.impl;

import gtranslator.domain.Dictionary;
import gtranslator.domain.Language;
import gtranslator.domain.Phonetic;
import gtranslator.domain.Transcription;
import gtranslator.domain.TranslateModel;
import gtranslator.domain.Word;
import gtranslator.gmodel.Dic;
import gtranslator.gmodel.Dics;
import gtranslator.gmodel.Entry;
import gtranslator.gmodel.Trans;
import gtranslator.service.GoogleReceiver;
import gtranslator.service.Holder;
import gtranslator.service.IvonaReceiver;
import gtranslator.service.OxfordReceiver;
import gtranslator.service.Translator;
import gtranslator.utils.Utils;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TranslatorImpl implements Translator {
    private static final Logger logger = LoggerFactory.getLogger(TranslatorImpl.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private Holder holder;

    @Autowired
    private OxfordReceiver oxfordReceiver;

    @Autowired
    private IvonaReceiver ivonaReceiver;

    @Autowired
    private GoogleReceiver googleReceiver;

    private TranslatorImpl syncTask = SyncTaskFactory.createFromClass(this);

    @Override
    public void translate(String source, Language srcLang, Language trgLang, Callback callback) {
        eventPublisher.publishEvent(new TranslateEvent(source, srcLang, trgLang, callback));
    }

    @EventListener
    public void handleTranslate(TranslateEvent event) {
        try {
            processTranslate(event.source, event.srcLang, event.trgLang, event.callback);
        } catch (Exception ex) {
            event.callback.onFailure(ex, "");
        }
    }

    private void processTranslate(String source, Language srcLang, Language trgLang, Callback callback) throws Exception {
        if (StringUtils.isBlank(source)) {
            logger.warn("TranslatorImpl:processTranslate: source is empty: " + source);
            return;
        }
        Word srcWord = holder.loadWord(source, srcLang);
        if (srcWord == null) {
            holder.saveWord(source, srcLang, null);
            syncTask.handleGoogle(source, srcLang, trgLang, callback);
            syncTask.handleOxford(source, srcLang, trgLang, callback);
            syncTask.handleIvona(source, srcLang, trgLang, callback);
            TranslateModel model = toModel(source, srcLang, Collections.emptyList());
            callback.onComplete(fillTranscriptionIfAbsent(model));
        } else {
            List<Dictionary> dictionaries = holder.loadDictionary(source, srcLang, trgLang);
            TranslateModel model = toModel(source, srcLang, dictionaries);
            if (model.getTranslations().isEmpty()) {
                syncTask.handleGoogle(source, srcLang, trgLang, callback);
            }
            if (model.getTranscriptions().isEmpty()) {
                syncTask.handleOxford(source, srcLang, trgLang, callback);
            }
            if (srcLang == Language.EN && (
                    holder.getAudioIvonaFile(source, Phonetic.AM) == null)
                    || holder.getAudioIvonaFile(source, Phonetic.BR) == null) {
                syncTask.handleIvona(source, srcLang, trgLang, callback);
            }
            callback.onComplete(fillTranscriptionIfAbsent(model));
        }
    }

    private TranslateModel toModel(String source, Language srcLang, List<Dictionary> dictionaries) throws IllegalAccessException, InstantiationException {
        if (dictionaries.isEmpty()) {
            TranslateModel model = new TranslateModel();
            model.setText(source);
            model.setLang(srcLang);
            return model;
        }
        TranslateModel model = new TranslateModel();
        Word srcWord1 = holder.loadWord(source, srcLang);
        Word srcWord = dictionaries.get(0).getSource();
        model.setText(srcWord.getText());
        model.setLang(srcWord.getLang());
        for (Transcription trn : srcWord.getTranscriptions()) {
            model.addTranscription(trn.getPhonetic(), trn.getText());
        }
        for (Dictionary dic : dictionaries) {
            model.addTranslation(dic.getTarget().getLang(), dic.getCategory(), dic.getTarget().getText(),
                    dic.getWeight());
        }
        return model;
    }

    @SyncTaskFactory.SyncTask
    @Transactional(propagation = Propagation.MANDATORY)
    public void handleOxford(String source, Language srcLang, Language trgLang, Callback callback) throws Exception {
        if (srcLang != Language.EN || !Utils.isSingleWord(source)) {
            return;
        }
        try {
            Map<String, Map<Phonetic, Set<String>>> result = oxfordReceiver.load(source);
            if (result.size() == 0) {
                return;
            }
            for (String word : result.keySet()) {
                Set<Transcription> transcriptions = new HashSet<>();
                for (Map.Entry<Phonetic, Set<String>> ent : result.get(word).entrySet()) {
                    for (String trn : ent.getValue()) {
                        Transcription transcription = new Transcription();
                        transcription.setPhonetic(ent.getKey());
                        transcription.setText(trn);
                        transcriptions.add(transcription);
                    }
                }
                holder.saveWord(word, srcLang, transcriptions);
            }
            List<Dictionary> dictionaries = holder.loadDictionary(source, srcLang, trgLang);
            if (dictionaries.isEmpty()) {
                return;
            }
            TranslateModel model = toModel(source, srcLang, dictionaries);
            model.setTag("oxford");
            callback.onComplete(fillTranscriptionIfAbsent(model));
        } catch (Exception ex) {
            logger.error("handleOxford: " + ex.getMessage(), ex);
            callback.onFailure(ex, "oxford");
        }
    }

    @SyncTaskFactory.SyncTask
    public void handleIvona(String source, Language srcLang, Language trgLang, Callback callback) {
        if (srcLang != Language.EN || !Utils.isSingleWord(source)) {
            return;
        }
        try {
            ivonaReceiver.load(source);
            TranslateModel model;
            List<Dictionary> dictionaries = holder.loadDictionary(source, srcLang, trgLang);
            if (dictionaries.isEmpty()) {
                model = new TranslateModel();
                model.setText(source);
                model.setLang(srcLang);
                return;
            } else {
                model = toModel(source, srcLang, dictionaries);
            }
            model.setTag("ivona");
            callback.onComplete(fillTranscriptionIfAbsent(model));
        } catch (Exception ex) {
            logger.error("handleIvona: " + ex.getMessage(), ex);
            callback.onFailure(ex, "ivona");
        }
    }

    @SyncTaskFactory.SyncTask
    @Transactional(propagation = Propagation.MANDATORY)
    public void handleGoogle(String text, Language srcLang, Language trgLang, Callback callback) throws Exception {
        Dics dics = null;
        try {
            dics = googleReceiver.translate(text, srcLang, trgLang);
            logger.info("got a translate from google");
            TranslateModel model = new TranslateModel();
            model.setText(text);
            model.setLang(srcLang);
            Word srcWord = holder.loadWord(text, srcLang);
            if (dics.getDics().isEmpty()) {
                for (Trans trans : dics.getTrans()) {
                    Word trgWord = holder.loadWord(trans.getTrans(), trgLang);
                    if (trgWord == null) {
                        trgWord = holder.saveWord(trans.getTrans(), trgLang, null);
                    }
                    holder.saveDictionary(srcWord, trgWord, "", BigDecimal.ONE);
                    model.addTranslation(trgLang, "", trans.getTrans(), BigDecimal.ONE);
                }
            } else {
                for (Dic dic : dics.getDics()) {
                    for (Entry ent : dic.getEntries()) {
                        Word trgWord = holder.loadWord(ent.getWord(), trgLang);
                        if (trgWord == null) {
                            trgWord = holder.saveWord(ent.getWord(), trgLang, null);
                        }
                        holder.saveDictionary(srcWord, trgWord, dic.getPos(), ent.getScore());
                        model.addTranslation(trgLang, dic.getPos(), ent.getWord(), ent.getScore());
                    }
                }
            }
            List<Dictionary> dictionaries = holder.loadDictionary(text, srcLang, trgLang);
            model = toModel(text, srcLang, dictionaries);
            model.setTag("google");
            callback.onComplete(model);
        } catch (Exception ex) {
            String input = String.format("params: %s,%s,%s. Raw text: %s",
                    text, srcLang, trgLang, dics == null ? "" : dics.getRawText());
            logger.error("error in handleGoogle; input: " + input + ", cause: " + ex.getMessage(), ex);
            ex.printStackTrace();
            callback.onFailure(new RuntimeException(
                    String.format("params: %s,%s,%s. Raw text: %s",
                            text, srcLang, trgLang, dics == null ? "" : dics.getRawText()), ex), "google");
        }
    }

    private TranslateModel fillTranscriptionIfAbsent(TranslateModel model) throws IllegalAccessException, InstantiationException {
        if (model.getLang() == Language.EN) {
            if (model.getTranscriptions().isEmpty()) {
                if (holder.getAudioIvonaFile(model.getText(), Phonetic.AM) != null) {
                    model.addTranscription(Phonetic.AM, "[" + model.getText() + "]");
                }
                if (holder.getAudioIvonaFile(model.getText(), Phonetic.BR) != null) {
                    model.addTranscription(Phonetic.BR, "[" + model.getText() + "]");
                }
            }
        }
        return model;
    }

    public static class TranslateEvent {
        final String source;
        final Language srcLang;
        final Language trgLang;
        final Callback callback;

        public TranslateEvent(String source, Language srcLang, Language trgLang, Callback callback) {
            this.source = source;
            this.srcLang = srcLang;
            this.trgLang = trgLang;
            this.callback = callback;
        }

        @Override
        public String toString() {
            return String.format("%s, %s, %s", source, srcLang, trgLang);
        }
    }
}