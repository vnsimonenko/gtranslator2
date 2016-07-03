package gtranslator.service.impl;

import gtranslator.domain.Phonetic;
import gtranslator.service.Holder;
import gtranslator.service.OxfordReceiver;
import gtranslator.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class OxfordReceiverImpl implements OxfordReceiver {
    private static Logger logger = Logger.getLogger(OxfordReceiver.class);
    private final static String USERAGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36";
    private static final String REQUEST = "http://www.oxfordlearnersdictionaries.com/definition/english/%1$s_1?q=%1$s";

    @Autowired
    private Holder holder;

    public Map<String, Map<Phonetic, Set<String>>> load(String enWord) {
        try {
            return capture(enWord);
        } catch (IOException | URISyntaxException | InstantiationException | IllegalAccessException ex) {
            logger.error(ex);
            if (ex instanceof HttpStatusException) {
                HttpStatusException het = (HttpStatusException) ex;
                if (het.getStatusCode() == 404) {
                    return Collections.emptyMap();
                }
            }
            throw new RuntimeException("OxfordReceiver:load: " + enWord, ex);
        }
    }

    private Map<String, Map<Phonetic, Set<String>>> capture(String enWord) throws IOException, URISyntaxException, IllegalAccessException, InstantiationException {
        Map<String, Map<Phonetic, Set<String>>> result = new HashMap<>();

        String request = String.format(REQUEST, enWord);
        Document doc = Jsoup.connect(request).timeout(30000).get();
        Elements elements = doc.select(
                "div[class=\"pron-link\"] a[href^=\"http://www.oxfordlearnersdictionaries.com/pronunciation/english/\"]");
        if (elements.size() == 0) {
            return result;
        }
        String phonRequest = elements.get(0).attr("href");
        Document phdoc = Jsoup.connect(phonRequest).timeout(3000).get();
        for (String[] phonView : new String[][]{{"NAmE", "us", Phonetic.AM.name()},
                {"BrE", "uk", Phonetic.BR.name()}}) {
            elements = phdoc.select("div[class=\"pron_row clear_fix\"]:has(span:contains(" + phonView[0]
                    + ")) div[class=\"pron_row__wrap1\"]:has(span:contains(" + phonView[0] + "))");
            for (Element el : elements) {
                Elements phElements = el
                        .select("span:contains(" + phonView[0] + ") + span[class=\"pron_phonetic\"]:has(pnc.wrap)");
                Elements sndElements = el
                        .select("div[class=\"sound audio_play_button pron-" + phonView[1] + " icon-audio\"]");
                String href = sndElements.get(0).attr("data-src-mp3");
                String word = getWordFromHref(href, Phonetic.AM.name().equals(phonView[2]));
                String transcription = "";
                try {
                    transcription = phElements.get(0).text().substring(1, phElements.get(0).text().length() - 1);
                } catch (IndexOutOfBoundsException ex) {
                    logger.error("fail in capture: " + enWord + ", url: " + request);
                }
                Phonetic phonetic = Phonetic.valueOf(phonView[2]);
                File f = holder.getAudioOxfordFile(word, transcription, phonetic);
                if (f == null) {
                    try (InputStream in = loadFile(href)) {
                        holder.saveAudioOxfordFile(in, phonetic, word, transcription);

                    } catch (IOException ex) {
                        logger.error(ex.getMessage() + "; URL: ".concat(request), ex);
                        throw ex;
                    }
                }
                addResult(word, Phonetic.valueOf(phonView[2]), transcription, result);
            }
        }
        return result;
    }

    private String getWordFromHref(String href, boolean isAm) {
        String word = href.substring(href.lastIndexOf('/') + 1, href.length() - 4);
        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile(String.format("^([^0-9]*)[_0-9]*%s[_0-9]*", isAm ? "_us" : "_gb"));
        Matcher matcher = pattern.matcher(word);
        if (matcher.find()) {
            word = matcher.group(1);
            while (word.endsWith("_")) {
                word = word.substring(0, word.length() - 1);
            }
        }
        return word.replace("_", "'");
    }

    private InputStream loadFile(String request) throws IOException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setDoOutput(false);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USERAGENT);
        conn.setUseCaches(true);
        return conn.getInputStream();
    }

    private void addResult(String word, Phonetic phonetic, String transcription, Map<String, Map<Phonetic, Set<String>>> result) throws InstantiationException, IllegalAccessException {
        Utils.getMultiplicityValueFromMap(
                Utils.getMultiplicityValueFromMap(result, word, HashMap::new),
                phonetic, HashSet::new).add(transcription);
    }
}
