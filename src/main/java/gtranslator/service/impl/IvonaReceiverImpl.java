package gtranslator.service.impl;

import gtranslator.domain.Phonetic;
import gtranslator.service.Holder;
import gtranslator.service.IvonaReceiver;
import gtranslator.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class IvonaReceiverImpl implements IvonaReceiver {
    private static Logger logger = LoggerFactory.getLogger(IvonaReceiverImpl.class);
    private final static String USERAGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36";

    @Autowired
    private Holder holder;

    /**
     * Load mp3 file from cloud of web resource of ivona
     *
     * @param source
     * @return IvonaReceiverResult
     * @throws IOException
     */
    public void load(String source) throws IOException {
        capture(source);
    }

    private Map<String, String> capture(String enWord) throws IOException {
        URL url = new URL("https://www.ivona.com/us/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USERAGENT);
        conn.setUseCaches(false);
        conn.setRequestProperty("Accept", "*/*");

        Document doc;
        try (InputStream in = conn.getInputStream()) {
            doc = Jsoup.parse(in, "UTF-8", "https://www.ivona.com");
        }
        String csrfield = doc.select("input[id=\"VoiceTesterForm_csrfield\"][name=\"csrfield\"][type=\"hidden\"]")
                .get(0).attr("value");

        String cookie = Utils.extractCookie(conn);

        Map<String, String> sndPaths = new HashMap<>();
        for (String[] phon : new String[][]{{Phonetic.AM.name(), "11"}, {Phonetic.BR.name(), "8"}}) {
            url = new URL("https://www.ivona.com/let-it-speak/?setLang=us");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", USERAGENT);
            conn.setUseCaches(false);
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");

            StringBuilder params = new StringBuilder();
            params.append("ext=mp3");
            params.append("&voiceSelector=");
            params.append(phon[1]);
            params.append("&text=");
            params.append(enWord);
            params.append("&send=Play");
            params.append("&csrfield=");
            params.append(csrfield);
            params.append("&ref-form-name=VoiceTesterForm");

            try (OutputStream output = conn.getOutputStream()) {
                output.write(params.toString().getBytes("UTF-8"));
            }

            String request;
            try (InputStream in = conn.getInputStream()) {
                JsonObject jsonObject = Json.createReader(in).readObject();
                request = jsonObject.getString("script");
                request = request.substring("window['voiceDemo'].audioUpdate('".length(), request.length() - 2);
            }

            cookie = Utils.extractCookie(conn);

            url = new URL(request);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);
            conn.setRequestProperty("User-Agent", USERAGENT);
            conn.setUseCaches(false);
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestProperty("X-Requested-With", "ShockwaveFlash/20.0.0.267");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");

            try (InputStream in = conn.getInputStream()) {
                File f = holder.saveAudioIvonaFile(in, Phonetic.valueOf(phon[0]), enWord + ".mp3");
                sndPaths.put(phon[0], f.getAbsolutePath());
            }
        }
        return sndPaths;
    }
}

