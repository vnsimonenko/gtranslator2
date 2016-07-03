package gtranslator.gmodel;

public class Trans {
    @com.google.api.client.util.Key("trans")
    private String trans;
    @com.google.api.client.util.Key("orig")
    private String orig;

    public String getTrans() {
        return trans;
    }

    public String getOrig() {
        return orig;
    }
}
