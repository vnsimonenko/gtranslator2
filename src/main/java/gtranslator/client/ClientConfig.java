package gtranslator.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {
    @Value(value = "${custom.prop.src.lang}")
    private String srcLang;

    @Value(value = "${custom.prop.trg.lang}")
    private String trgLang;

    @Value(value = "${custom.prop.amount_view_words}")
    private String amountViewWords;

    @Value(value = "${custom.prop.auto.play.am}")
    private volatile Boolean amAutoPlay;

    @Value(value = "${custom.prop.auto.play.br}")
    private volatile Boolean brAutoPlay;
}
