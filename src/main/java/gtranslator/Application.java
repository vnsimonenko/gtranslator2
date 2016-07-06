package gtranslator;

import gtranslator.client.ClipboardHelper;
import gtranslator.client.HtmlHelper;
import gtranslator.client.ToolApplication;
import gtranslator.domain.Language;
import gtranslator.domain.Phonetic;
import gtranslator.domain.TranslateModel;
import gtranslator.service.Holder;
import gtranslator.service.Translator;
import gtranslator.utils.AudioHelper;
import gtranslator.utils.Utils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableCaching
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class Application implements CommandLineRunner, PropertyChangeListener, ApplicationContextAware {
    private static final Logger logger = Logger.getLogger(Application.class);
    private static ConfigurableApplicationContext context;
    /**
     * records events from different modules. observer.
     */
    public final static PropertySupport PROPERTYSUPPORT = new PropertySupport();
    private ToolApplication toolApplication;
    private volatile String selectedText;
    private boolean hasTranscriptions;
    private volatile String lastAutoPlayText;
    private volatile TranslateModel lastModel;
    private ExecutorService audioExecutor = Executors.newSingleThreadExecutor();

    @Autowired
    private Translator translator;

    @Autowired
    private Holder holder;

    @Autowired
    private DefApplicationSettings applicationSettings;

    private volatile Language srcLang;
    private volatile Language trgLang;
    private volatile int amountViewWords;
    private volatile boolean autoPlayAm;
    private volatile boolean autoPlayBr;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebEnvironment(false);
        app.setHeadless(false);
        app.run(args);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertySupport.Property.EXIT.equalsEvent(evt)) {
            Arrays.asList(PROPERTYSUPPORT.pcs.getPropertyChangeListeners())
                    .forEach(PROPERTYSUPPORT.pcs::removePropertyChangeListener);
            if (context.isActive()) {
                context.close();
            }
        }
        if (Application.PropertySupport.Property.SRC_LANG.equalsEvent(evt)) {
            srcLang = (Language) evt.getNewValue();
        } else if (Application.PropertySupport.Property.TRG_LANG.equalsEvent(evt)) {
            trgLang = (Language) evt.getNewValue();
        } else if (Application.PropertySupport.Property.AM_PLAY.equalsEvent(evt)) {
            play(selectedText, (String) evt.getNewValue(), Phonetic.AM);
        } else if (Application.PropertySupport.Property.BR_PLAY.equalsEvent(evt)) {
            play(selectedText, (String) evt.getNewValue(), Phonetic.BR);
        } else if (Application.PropertySupport.Property.AM_AUTO_PLAY.equalsEvent(evt)) {
            autoPlayAm = (Boolean) evt.getNewValue();
        } else if (Application.PropertySupport.Property.BR_AUTO_PLAY.equalsEvent(evt)) {
            autoPlayBr = (Boolean) evt.getNewValue();
        } else if (PropertySupport.Property.AMOUNT_VIEW_WORDS.equalsEvent(evt)) {
            amountViewWords = (Integer) evt.getNewValue();
        } else if (PropertySupport.Property.COPY_TO_CLIPBOARD.equalsEvent(evt)) {
            copyToClipboard((String) evt.getNewValue());
        }
    }

    @Override
    public void run(String... strings) throws Exception {
        Application.PROPERTYSUPPORT.addPropertyChangeListener(this);
        srcLang = applicationSettings.getSrcLang();
        trgLang = applicationSettings.getTrgLang();
        autoPlayAm = applicationSettings.isAutoPlayAm();
        autoPlayBr = applicationSettings.isAutoPlayBr();
        amountViewWords = applicationSettings.getAmountViewWords();
        start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = (ConfigurableApplicationContext) applicationContext;
        context.addApplicationListener(new ApplicationListener<ContextClosedEvent>() {
            @Override
            public void onApplicationEvent(ContextClosedEvent event) {
                ClipboardHelper.INSTANCE.close();
                Platform.exit();
            }
        });
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }

    public static class PropertySupport {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            this.pcs.addPropertyChangeListener(listener);
        }

        public synchronized void firePropertyChange(Property property, Object oldValue,
                                                    Object newValue) {
            pcs.firePropertyChange(property.name(), oldValue, newValue);
        }

        public enum Property {
            SRC_LANG, TRG_LANG, AMOUNT_VIEW_WORDS, AMOUNT_CHARS, ACTIVE, EXIT, HISTORY,
            MODE, AM_AUTO_PLAY, BR_AUTO_PLAY, AM_PLAY, BR_PLAY, COPY_TO_CLIPBOARD;

            public boolean equalsEvent(PropertyChangeEvent evt) {
                return name().equals(evt.getPropertyName());
            }
        }
    }

    public void start() throws Exception {
        toolApplication = ToolApplication.getInstance();

        final LinkedBlockingQueue<ClipboardHelper.ActionListener.ActionEvent> queue = new LinkedBlockingQueue<>();

        ClipboardHelper.INSTANCE.addActionListener((event) -> queue.add(event));

        Thread thread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    ClipboardHelper.ActionListener.ActionEvent event = queue.take();
                    hasTranscriptions = false;

                    selectedText = Utils.normalText(event.getText());
                    if (StringUtils.isBlank(selectedText)) {
                        Platform.runLater(() -> {
                            try {
                                if (StringUtils.isBlank(selectedText)) {
                                    toolApplication.getPopupWindow().hide();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        continue;
                    }

                    translator.translate(selectedText, srcLang, trgLang, new Translator.Callback() {
                        @Override
                        public void onComplete(TranslateModel model) {
                            try {
                                if (!StringUtils.defaultString(selectedText, "").equals(model.getText())) {
                                    return;
                                }

                                if (autoPlayAm || autoPlayBr) {
                                    audioExecutor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                playAuto(model);
                                            } catch (UnsupportedEncodingException e) {
                                            }
                                        }
                                    });
                                }

                                if ("google".equalsIgnoreCase(model.getTag())) {
                                    lastModel = model;
                                }
                                if ("ivona".equalsIgnoreCase(model.getTag())) {
                                    if (hasTranscriptions) return;
                                }
                                hasTranscriptions = !model.getTranscriptions().isEmpty();

                                final String html = HtmlHelper.toHtml(model, amountViewWords);
                                Platform.runLater(() -> {
                                    try {
                                        if (!StringUtils.isBlank(event.getText())) {
                                            toolApplication.getPopupWindow().show(html, event.getX(), event.getY());
                                        } else if (toolApplication.getPopupWindow().canHideWindow(event.getX(), event.getY(), event.getClickCount())) {
                                            toolApplication.getPopupWindow().hide();
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Exception ex, String tag) {
                            ex.printStackTrace();
                            Platform.runLater(() -> {
                                if ("google".equalsIgnoreCase(tag)) {
                                    TranslateModel model = new TranslateModel();
                                    model.setText(ex.getMessage());
                                    model.setLang(srcLang);
                                    toolApplication.getPopupWindow().hide();
                                    try {
                                        model.addTranslation(Language.EN, "ERROR", ex.getMessage(), BigDecimal.ZERO);
                                        toolApplication.getPopupWindow().show(HtmlHelper.toHtml(model, 0));
                                    } catch (Exception ex2) {
                                    }
                                }
                            });
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
        ClipboardHelper.INSTANCE.setMode(applicationSettings.getMode());
        ClipboardHelper.INSTANCE.setAmountChars(applicationSettings.getAmountChars());
        ClipboardHelper.INSTANCE.setActive(true);
    }

    private boolean play(String word, String transcription, Phonetic phonetic) {
        try {
            File f = StringUtils.isBlank(transcription) ? null : holder.getAudioOxfordFile(selectedText, transcription, phonetic);
            if (f == null) {
                f = holder.getAudioIvonaFile(word, phonetic);
            }
            if (f != null) {
                AudioHelper.play(f.getAbsolutePath());
            }
            return f != null;
        } catch (UnsupportedEncodingException ex) {
            return false;
        }
    }

    private void playAuto(TranslateModel model) throws UnsupportedEncodingException {
        if (StringUtils.defaultString(lastAutoPlayText, "").equalsIgnoreCase(selectedText)) {
            return;
        }

        try {
            if (autoPlayAm) {
                Set<String> wordsAm = model.getTranscriptions().get(Phonetic.AM);
                String am = wordsAm == null || wordsAm.isEmpty() ? null : wordsAm.iterator().next();
                if (play(model.getText(), am, Phonetic.AM)) {
                    lastAutoPlayText = selectedText;
                }
            }
            if (autoPlayBr) {
                Set<String> wordsBr = model.getTranscriptions().get(Phonetic.BR);
                String br = wordsBr == null || wordsBr.isEmpty() ? null : wordsBr.iterator().next();
                if (play(model.getText(), br, Phonetic.BR)) {
                    lastAutoPlayText = selectedText;
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void copyToClipboard(String html) {
        String text = null;
        if (lastModel != null) {
            try {
                text = HtmlHelper.toText(lastModel, amountViewWords);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        if (!StringUtils.isBlank(text) || !StringUtils.isBlank(html)) {
            ClipboardHelper.INSTANCE.copyTextToClipboard(StringUtils.isBlank(text)
                    ? html : text);
        }
    }
}
