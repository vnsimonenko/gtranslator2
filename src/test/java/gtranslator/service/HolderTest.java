package gtranslator.service;

import gtranslator.Application;
import gtranslator.domain.Dictionary;
import gtranslator.domain.Language;
import gtranslator.domain.Phonetic;
import gtranslator.domain.Transcription;
import gtranslator.domain.Word;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
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
public class HolderTest {

    @Autowired
    private Holder holder;

    @Test
    public void testSaveDictionary() throws Exception {
        Set<Transcription> transcriptions1 = new HashSet<>();
        Set<Transcription> transcriptions2 = new HashSet<>();
        transcriptions1.add(new Transcription("trn1", Phonetic.AM));
        transcriptions1.add(new Transcription("trn2", Phonetic.AM));
        transcriptions2.add(new Transcription("trn3", Phonetic.BR));
        Word word1 = holder.saveWord("texta", Language.EN, transcriptions1);
        Word word2 = holder.saveWord("textb", Language.EN, transcriptions2);
        Word word3 = holder.saveWord("textc", Language.EN, transcriptions2);
        holder.saveDictionary(word1, word2, "NOUN", BigDecimal.ONE);
        holder.saveDictionary(word1, word3, "VERB", BigDecimal.ONE);
        Dictionary dictionary = holder.loadDictionary("texta", Language.EN, "textb", Language.EN);
        Assert.assertEquals("texta", dictionary.getSource().getText());
        Assert.assertEquals("textb", dictionary.getTarget().getText());
        dictionary = holder.loadDictionary("texta", Language.EN, "textc", Language.EN);
        Assert.assertEquals("texta", dictionary.getSource().getText());
        Assert.assertEquals("textc", dictionary.getTarget().getText());
    }
}