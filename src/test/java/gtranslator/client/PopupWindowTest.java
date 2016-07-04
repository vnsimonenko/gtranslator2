package gtranslator.client;

import gtranslator.domain.Language;
import gtranslator.domain.Phonetic;
import gtranslator.domain.TranslateModel;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PopupWindowTest {
    @Test
    public void demoPopup() throws Exception {
        final ToolApplication tool = ToolApplication.getInstance();

        final LinkedBlockingQueue<ClipboardHelper.ActionListener.ActionEvent> lists = new LinkedBlockingQueue<>();

        ClipboardHelper.INSTANCE.addActionListener((event) -> {
            lists.add(event);
        });

        Thread thread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    ClipboardHelper.ActionListener.ActionEvent event = lists.take();

                    Platform.runLater(() -> {
                        try {
                            if (!StringUtils.isBlank(event.getText())) {
                                tool.getPopupWindow().show(toHtml(event.getText()), event.getX(), event.getY());
                            } else if (tool.getPopupWindow().canHideWindow(event.getX(), event.getY(), event.getClickCount())) {
                                tool.getPopupWindow().hide();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        ClipboardHelper.INSTANCE.setMode(ClipboardHelper.MODE.SELECT);
        ClipboardHelper.INSTANCE.setActive(true);

        Thread.sleep(100000);
        ClipboardHelper.INSTANCE.close();
    }

    @Test
    public void demoPopup2() throws Exception {
        final ToolApplication tool = ToolApplication.getInstance();

        final LinkedBlockingQueue<ClipboardHelper.ActionListener.ActionEvent> lists = new LinkedBlockingQueue<>();

        ClipboardHelper.INSTANCE.addActionListener((event) -> {
            lists.add(event);
        });

        Thread thread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    ClipboardHelper.ActionListener.ActionEvent event = lists.take();

                    Platform.runLater(() -> {
                        try {
                            if (!StringUtils.isBlank(event.getText())) {
                                tool.getPopupWindow().show(toHtml(event.getText()), event.getX(), event.getY());
                            } else if (tool.getPopupWindow().canHideWindow(event.getX(), event.getY(), event.getClickCount())) {
                                tool.getPopupWindow().hide();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        ClipboardHelper.INSTANCE.setMode(ClipboardHelper.MODE.SELECT);
        ClipboardHelper.INSTANCE.setActive(true);

        Thread.sleep(100000);
        ClipboardHelper.INSTANCE.close();
    }

    public String toHtml(String w) throws Exception {
        TranslateModel model = new TranslateModel();
        model.setText("test");
        model.setLang(Language.EN);
        String[] ws = w.split("[ ]");
        BigDecimal weight = new BigDecimal("1.0");
        for (String word : ws) {
            model.addTranslation(Language.RU, "NOUN", word.trim().toLowerCase(), weight);
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
        String html = HtmlHelper.toHtml(model, 10);
        ;
        Path path = Paths.get("/tmp", "test.html");
        Files.copy(new ByteArrayInputStream(html.getBytes("utf-8")), path, StandardCopyOption.REPLACE_EXISTING);
        return HtmlHelper.toHtml(model, 10);
    }

    public String toHtml2(String w) throws Exception {
        TranslateModel model = new TranslateModel();
        model.setText("test");
        model.setLang(Language.EN);
        BigDecimal weight = new BigDecimal("1.0");
        model.addTranslation(Language.RU, "", w.trim().toLowerCase(), weight);
        String html = HtmlHelper.toHtml(model, 10);
        Path path = Paths.get("/tmp", "test2.html");
        Files.copy(new ByteArrayInputStream(html.getBytes("utf-8")), path, StandardCopyOption.REPLACE_EXISTING);
        return HtmlHelper.toHtml(model, 10);
    }
}
