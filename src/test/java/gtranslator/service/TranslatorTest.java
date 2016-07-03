package gtranslator.service;

import gtranslator.Application;
import gtranslator.domain.Language;
import gtranslator.domain.TranslateModel;
import gtranslator.gmodel.Dics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TranslatorTest {

    @Autowired
    private GoogleReceiver googleReceiver;

    @Autowired
    private Translator translator;

    @Test
    public void testTranslate() throws Exception {
        Dics dics = googleReceiver.translate("test", Language.EN, Language.RU);
        Assert.assertTrue(dics.getDics().size() > 0);
    }

    @Test
    public void test2() throws Exception {
        translator.translate("be", Language.EN, Language.RU, new Translator.Callback() {
            @Override
            public void onComplete(TranslateModel model) {

            }

            @Override
            public void onFailure(Exception ex, String tag) {
                ex.printStackTrace();
            }
        });

        Thread.sleep(100000);

    }
}