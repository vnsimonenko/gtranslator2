package gtranslator.gmodel;

import java.util.List;

/**
 * Created by vns on 15.06.16.
 */
public class Dic {
    @com.google.api.client.util.Key("pos")
    private String pos;
    @com.google.api.client.util.Key("entry")
    private List<Entry> entries;

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}
