package gtranslator;

import gtranslator.client.ClipboardHelper;
import gtranslator.domain.Language;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class DefApplicationSettings {
    @Value("${custom.prop.src.lang:en}")
    private volatile Language srcLang;
    @Value("${custom.prop.trg.lang:ru}")
    private volatile Language trgLang;
    @Value("${custom.prop.amount_view_words:10}")
    private volatile int amountViewWords;
    @Value("${custom.prop.amount_chars:100}")
    private volatile int amountChars;
    @Value("${custom.prop.mode:copy}")
    private volatile ClipboardHelper.MODE mode;
    @Value("${custom.prop.auto.play.am:false}")
    private volatile boolean autoPlayAm;
    @Value("${custom.prop.auto.play.br:false}")
    private volatile boolean autoPlayBr;
    @Value("${custom.prop.audioIvonaDir:systemProperties['user.home']}")
    private volatile String audioIvonaDir;
    @Value("${custom.prop.audioIvonaDir:systemProperties['user.home']}")
    private volatile String audioOxfordDir;

    public Language getSrcLang() {
        return srcLang;
    }

    public Language getTrgLang() {
        return trgLang;
    }

    public int getAmountViewWords() {
        return amountViewWords;
    }

    public int getAmountChars() {
        return amountChars;
    }

    public ClipboardHelper.MODE getMode() {
        return mode;
    }

    public boolean isAutoPlayAm() {
        return autoPlayAm;
    }

    public boolean isAutoPlayBr() {
        return autoPlayBr;
    }

    public String getAudioIvonaDir() {
        return audioIvonaDir;
    }

    public String getAudioOxfordDir() {
        return audioOxfordDir;
    }

    @Bean
    public static ConversionService conversionService() {
        final FormattingConversionService service = new DefaultFormattingConversionService();
        service.addConverter(String.class, Language.class, new Converter<String, Language>() {
            @Override
            public Language convert(String s) {
                return Language.valueOf(s.toUpperCase());
            }
        });
        service.addConverter(String.class, ClipboardHelper.MODE.class, new Converter<String, ClipboardHelper.MODE>() {
            @Override
            public ClipboardHelper.MODE convert(String s) {
                return ClipboardHelper.MODE.valueOf(s.toUpperCase());
            }
        });
        return service;
    }
}