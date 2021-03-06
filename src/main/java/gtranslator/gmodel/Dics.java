package gtranslator.gmodel;

import java.util.Collections;
import java.util.List;

public class Dics {
    @com.google.api.client.util.Key("sentences")
    private java.util.List<Trans> trans;
    @com.google.api.client.util.Key("dict")
    private java.util.List<Dic> dics;
    private String rawText;

    public List<Dic> getDics() {
        return dics == null ? Collections.emptyList() : dics;
    }

    public void setDics(List<Dic> dics) {
        this.dics = dics;
    }

    public List<Trans> getTrans() {
        return trans == null ? Collections.emptyList() : trans;
    }

    public void setTrans(List<Trans> trans) {
        this.trans = trans;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}
