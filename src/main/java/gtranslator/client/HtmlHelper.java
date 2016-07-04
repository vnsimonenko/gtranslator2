package gtranslator.client;

import gtranslator.domain.TranslateModel;
import gtranslator.utils.JsonTransformer;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.EnumSet;
import javax.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import sun.swing.SwingUtilities2;

public class HtmlHelper {
    public static String toHtml(TranslateModel model, int maxCount) throws Exception {
        String text = StringUtils.defaultString(model.getText(), "").trim();
        boolean isText = text.split("[ ]").length > 1;
        JsonObject jsonObject = model.toJson(isText
                ? EnumSet.of(TranslateModel.Fields.TRANSLATIONS)
                : EnumSet.of(TranslateModel.Fields.TRANSLATIONS, TranslateModel.Fields.TRANSCRIPTIONS));
        JsonTransformer transformer = JsonTransformer.createJsonTransformer();
        transformer.setMaxCount(maxCount);
        if (isText) {
            Point point = calculateSize(text);
            transformer.setHeight(point.y);
            transformer.setWidth(point.x);
        } else {
            transformer.setHeight(10);
            transformer.setWidth(10);
        }
        return transformer.convertJsonToHtml(jsonObject.toString(),
                isText ? JsonTransformer.XSL.TEXT : JsonTransformer.XSL.WORD);
    }

    private static Point calculateSize(String text) {
        Font f = new Font("monospace", 0, 13);
        double koef = 1.0 / 3.0;
        FontMetrics fm = SwingUtilities2.getFontMetrics(null, f);
        double h = Math.sqrt(fm.stringWidth(text) * fm.getHeight() * koef);
        double w = h / koef;
        Dimension wsz = Toolkit.getDefaultToolkit().getScreenSize();
        if (w > wsz.getWidth()) {
            w = wsz.getWidth() * 0.5;
        }
        if (h > wsz.getHeight()) {
            h = wsz.getHeight() * 0.5;
        }
        Point point = new Point((int) w, (int) h);
        return point;
    }
}
