package gtranslator.service;

import gtranslator.Application;
import gtranslator.domain.Language;
import gtranslator.domain.TranslateModel;
import gtranslator.gmodel.Dics;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TranslatorTest {

    @Autowired
    private GoogleReceiver googleReceiver;

    @Autowired
    private Translator translator;

    @Test
    public void testGoogle() throws Exception {
        Dics dics = googleReceiver.translate("be", Language.EN, Language.RU);
        Assert.assertTrue(dics.getDics().size() > 0);
    }

    private TranslateModel translateModel;

    @Test
    public void testCallback() throws Exception {
        final CountDownLatch downLatch = new CountDownLatch(1);
        translator.translate("be", Language.EN, Language.RU, new Translator.Callback() {
            @Override
            public void onComplete(TranslateModel model) {
                if ("google".equalsIgnoreCase(model.getTag())) {
                    translateModel = model;
                    downLatch.countDown();
                }
            }

            @Override
            public void onFailure(Exception ex, String tag) {
                if ("google".equalsIgnoreCase(tag)) {
                    ex.printStackTrace();
                    downLatch.countDown();
                }
            }
        });
        downLatch.await(60000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(translateModel.getTranslations().size() > 0);
    }
}