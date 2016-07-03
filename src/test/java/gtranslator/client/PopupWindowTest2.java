package gtranslator.client;

import gtranslator.Application;
import gtranslator.ApplicationConfig;
import gtranslator.domain.Language;
import gtranslator.domain.Phonetic;
import gtranslator.domain.TranslateModel;
import gtranslator.service.Translator;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import static java.lang.System.out;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, ApplicationConfig.class})
@TestPropertySource(locations = "classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PopupWindowTest2 {
    {
        out.println(java.awt.GraphicsEnvironment.isHeadless());
    }

    private Translator translator;

    @Autowired
    private Application application;

    @Before
    public void up() {
        translator = Mockito.mock(Translator.class);
        ReflectionTestUtils.setField(application, "translator", translator);
        doAnswer(invocationOnMock -> {
            TranslateModel model1 = dynAnswer(invocationOnMock);
            Translator.Callback callback = (Translator.Callback) invocationOnMock.getArguments()[3];
            callback.onComplete(model1);
            return null;
        }).when(translator).translate(anyString(), Matchers.eq(Language.EN), Matchers.eq(Language.RU), any(Translator.Callback.class));
    }

    @Test
    public void demoPopup() throws Exception {
        application.start();

        Thread.sleep(100000);
    }

    public static TranslateModel dynAnswer(InvocationOnMock invocationOnMock) throws Throwable {
        String s = invocationOnMock.getArguments()[0].toString();
        Language sl = invocationOnMock.getArguments().length > 1 ? (Language) invocationOnMock.getArguments()[1] : null;
        Language tl = invocationOnMock.getArguments().length > 2 ? (Language) invocationOnMock.getArguments()[2] : null;
        if (s.indexOf(" ") != -1) {
            return createTextModel(s, sl);
        } else {
            return createWordModel(s, sl);
        }
    }

    static TranslateModel createWordModel(String text, Language srcLang) throws IllegalAccessException, InstantiationException {
        TranslateModel model = new TranslateModel();
        model.setText(text);
        model.setLang(srcLang);
        String[] ws = new String[]{"test1", "test2", "test3", "test4", "test5"};
        BigDecimal weight = new BigDecimal("1.0");
        for (String word : ws) {
            model.addTranslation(Language.RU, "NOUN", text + "-" + word.trim().toLowerCase(), weight);
            weight = weight.subtract(new BigDecimal("0.1"));
        }
        weight = new BigDecimal("1.0");
        for (String word : ws) {
            model.addTranslation(Language.RU, "VERB", word.trim().toLowerCase(), weight);
            weight = weight.subtract(new BigDecimal("0.1"));
        }
        model.addTranscription(Phonetic.AM, "test1");
        model.addTranscription(Phonetic.AM, "test2");
        model.addTranscription(Phonetic.BR, "test3");
        return model;
    }

    static TranslateModel createTextModel(String text, Language srcLang) throws IllegalAccessException, InstantiationException {
        TranslateModel model = new TranslateModel();
        model.setText(text);
        model.setLang(srcLang);
        BigDecimal weight = new BigDecimal("1.0");
        model.addTranslation(Language.RU, "", text, weight);
        return model;
    }
}
