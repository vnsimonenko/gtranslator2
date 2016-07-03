package gtranslator.client;

import gtranslator.DefApplicationSettings;
import gtranslator.domain.Language;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.concurrent.CountDownLatch;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLInputElement;

public class ToolApplication extends Application implements PropertyChangeListener {
    private static volatile ToolApplication instance;
    //final is guarantee that instance will be created only once
    private static final CountDownLatch downLatch = new CountDownLatch(1);
    private PopupWindow popupWindow;
    private static int WIDTH = 230;
    private static int HEIGHT = 550;

    static {
        URL.setURLStreamHandlerFactory(protocol -> {
            return "resource".equals(protocol) ? new URLStreamHandler() {
                protected URLConnection openConnection(URL url) throws IOException {
                    return new ResourceURLConnection(url);
                }
            } : null; //default handler
        });
    }

    @Autowired
    private DefApplicationSettings applicationSettings;

    private volatile Language srcLang;
    private volatile Language trgLang;
    private volatile int amountViewWords;
    private volatile int amountChars;
    private volatile boolean autoPlayAm;
    private volatile boolean autoPlayBr;
    private ClipboardHelper.MODE mode;

    private volatile boolean enabled = true;

    public static ToolApplication getInstance() {
        if (instance == null) {
            synchronized (downLatch) {
                if (instance != null) {
                    return instance;
                }
                Thread thread = new Thread(() -> {
                    //would be called only once for fx engine otherwise will be thrown to exception.
                    //downLatch.toString() - a indicator of called the method getInstance
                    Application.launch(ToolApplication.class, "--singelton=" + downLatch.toString());
                });
                thread.start();
                try {
                    downLatch.await();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    //TODO RuntimeException
                    throw new RuntimeException(ex);
                }
            }
        }
        return instance;
    }

    @Override
    public void init() throws Exception {
        gtranslator.Application.getContext().getBeanFactory().autowireBean(this);
        gtranslator.Application.PROPERTYSUPPORT.addPropertyChangeListener(this);
        srcLang = applicationSettings.getSrcLang();
        trgLang = applicationSettings.getTrgLang();
        autoPlayAm = applicationSettings.isAutoPlayAm();
        autoPlayBr = applicationSettings.isAutoPlayBr();
        amountViewWords = applicationSettings.getAmountViewWords();
        amountChars = applicationSettings.getAmountChars();
        mode = applicationSettings.getMode();
    }

    @Override
    public void start(Stage primaryStage) {
        //Group root = new Group();

        Tab setupTab = new Tab("Setup");
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/client/images/setup.png")));
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        setupTab.setGraphic(imageView);
        setupTab.setContent(new Browser(WIDTH, HEIGHT));
        setupTab.setClosable(false);
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                setupTab
        );

        gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                gtranslator.Application.PropertySupport.Property.MODE, null, applicationSettings.getMode());

        primaryStage.setTitle("Google translator");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/client/images/fish.png")));
        primaryStage.setIconified(false);
        Scene scene = new Scene(tabPane, WIDTH, HEIGHT, Color.web("#666970"));
        scene.getStylesheets().add("client/css/fx.css");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
        primaryStage.centerOnScreen();

        popupWindow = new PopupWindow(tabPane);

        if (instance == null && downLatch.getCount() == 1
                && downLatch.toString().equals(getParameters().getNamed().get("singelton"))) {
            instance = this;
            downLatch.countDown();
        }
    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (gtranslator.Application.PropertySupport.Property.SRC_LANG.equalsEvent(evt)) {
            srcLang = (Language) evt.getNewValue();
        } else if (gtranslator.Application.PropertySupport.Property.TRG_LANG.equalsEvent(evt)) {
            trgLang = (Language) evt.getNewValue();
        } else if (gtranslator.Application.PropertySupport.Property.AMOUNT_VIEW_WORDS.equalsEvent(evt)) {
            amountViewWords = Integer.parseInt(evt.getNewValue().toString());
        }
    }

    class Browser extends Region {
        private int width;
        private int height;
        private final WebView browser = new WebView();
        private final WebEngine webEngine = browser.getEngine();

        public Browser(int width, int height) {
            this.width = width;
            this.height = height;
            getStyleClass().add("browser");

            webEngine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<Worker.State>() {
                        @Override
                        public void changed(ObservableValue<? extends Worker.State> ov,
                                            Worker.State oldState, Worker.State newState) {
                            if (newState == Worker.State.SUCCEEDED) {
                                JSObject win =
                                        (JSObject) webEngine.executeScript("window");
                                win.setMember("app", new JavaApp());
                            }
                        }
                    }
            );

            webEngine.getLoadWorker().stateProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue == Worker.State.SUCCEEDED) {
                            try {
                                ElementHelper elh = new ElementHelper(webEngine.getDocument());

                                HTMLInputElement el = elh.getElementById(ToolApplication.this.srcLang.name().toLowerCase() + "1");
                                if (el != null) {
                                    el.setChecked(true);
                                    el = elh.getElementById(ToolApplication.this.trgLang.name().toLowerCase() + "2");
                                    el.setChecked(true);
                                    el = elh.getElementById("amountwords");
                                    el.setValue(Integer.toString(ToolApplication.this.amountViewWords));
                                    elh.addEventListener("amountwords_but", "click", evt -> {
                                        HTMLInputElement amount = elh.getElementById("amountwords");
                                        try {
                                            gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                                                    gtranslator.Application.PropertySupport.Property.AMOUNT_VIEW_WORDS,
                                                    0, Integer.valueOf(amount.getValue()));
                                        } catch (NumberFormatException ex) {
                                            //
                                        }
                                    }, false);
                                    el = elh.getElementById("amountchars");
                                    el.setValue(Integer.toString(ToolApplication.this.amountChars));
                                    elh.addEventListener("amountchars_but", "click", evt -> {
                                        HTMLInputElement amount = elh.getElementById("amountchars");
                                        try {
                                            gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                                                    gtranslator.Application.PropertySupport.Property.AMOUNT_CHARS,
                                                    0, Integer.valueOf(amount.getValue()));
                                        } catch (NumberFormatException ex) {
                                            //
                                        }
                                    }, false);
                                    //type of translation
                                    for (String id : new String[]{"en1", "ru1", "ua1", "en2", "ru2", "ua2"}) {
                                        elh.addEventListener(id, "change", evt -> {
                                            gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                                                    id.endsWith("1")
                                                            ? gtranslator.Application.PropertySupport.Property.SRC_LANG
                                                            : gtranslator.Application.PropertySupport.Property.TRG_LANG,
                                                    null, Language.valueOf(id.substring(0, id.length() - 1).toUpperCase()));
                                        }, false);
                                    }
                                    // mode
                                    org.w3c.dom.events.EventListener listener = evt -> {
                                        HTMLInputElement elt = HTMLInputElement.class.cast(evt.getTarget());
                                        gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                                                gtranslator.Application.PropertySupport.Property.MODE,
                                                null, ClipboardHelper.MODE.valueOf(elt.getValue().toUpperCase()));
                                    };
                                    el = elh.getElementById("copyid");
                                    el.setChecked(ToolApplication.this.mode == ClipboardHelper.MODE.COPY);
                                    elh.addEventListener(el, "change", listener, false);
                                    el = elh.getElementById("selectid");
                                    el.setChecked(ToolApplication.this.mode == ClipboardHelper.MODE.SELECT);
                                    elh.addEventListener(el, "change", listener, false);
                                    //play
                                    listener = evt -> {
                                        HTMLInputElement elt = HTMLInputElement.class.cast(evt.getTarget());
                                        gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                                                gtranslator.Application.PropertySupport.Property.valueOf(elt.getValue()),
                                                null, elt.getChecked());
                                    };
                                    el = elh.getElementById("playamid");
                                    el.setChecked(ToolApplication.this.autoPlayAm);
                                    elh.addEventListener(el, "change", listener, false);
                                    el = elh.getElementById("playbrid");
                                    el.setChecked(ToolApplication.this.autoPlayBr);
                                    elh.addEventListener(el, "change", listener, false);
                                    el = elh.getElementById("enabledid");
                                    el.setChecked(ToolApplication.this.enabled);
                                    listener = evt -> {
                                        HTMLInputElement elt = HTMLInputElement.class.cast(evt.getTarget());
                                        enabled = elt.getChecked();
                                    };
                                    elh.addEventListener(el, "change", listener, false);
                                }
                                //browser.webEngine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
                            } catch (JSException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

            setOnMouseEntered(event -> gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                    gtranslator.Application.PropertySupport.Property.ACTIVE,
                    true, false));
            setOnMouseExited(event -> gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                    gtranslator.Application.PropertySupport.Property.ACTIVE,
                    false, enabled ? true : false));

            // load the home page
            webEngine.load(getClass().getResource("/client/app.html").toExternalForm());
            getChildren().add(browser);
        }

        public class JavaApp {
            public void exit() {
                gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                        gtranslator.Application.PropertySupport.Property.EXIT,
                        false, true
                );
            }
        }

        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
        }

        @Override
        protected double computePrefWidth(double height) {
            return this.width;
        }

        @Override
        protected double computePrefHeight(double width) {
            return this.height;
        }
    }

    public static class ElementHelper {
        private Document doc;

        public ElementHelper(Document doc) {
            this.doc = doc;
        }

        <T> T getElementById(String id) {
            return (T) doc.getElementById(id);
        }

        void addEventListener(String id, String eventType, org.w3c.dom.events.EventListener listener, boolean useCapture) {
            ((EventTarget) doc.getElementById(id)).addEventListener(eventType, listener, useCapture);
        }

        void addEventListener(final Object obj, String eventType, org.w3c.dom.events.EventListener listener, boolean useCapture) {
            ((EventTarget) obj).addEventListener(eventType, listener, useCapture);
        }
    }
}
