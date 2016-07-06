package gtranslator.client;

import com.sun.deploy.config.JfxRuntime;
import gtranslator.Application;
import java.awt.Insets;
import java.awt.Rectangle;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingNode;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import netscape.javascript.JSException;
import org.apache.log4j.Logger;

public class PopupWindow {
    private static final Logger logger = Logger.getLogger(ClipboardHelper.class);
    private static final int OFFSET_DY = 20;

    private ContextMenu contextMenu;
    private WebView webView;
    private int x;
    private int y;

    PopupWindow(final Node anchNode) {
        webView = new WebView();
        webView.getEngine().setOnStatusChanged(event -> {
            if ("WEB_STATUS_CHANGED".equals(event.getEventType().getName())) {
                adjustSizeAndShow(anchNode);
            }
        });
        webView.setStyle("-fx-background-color:#FFFFFF;-fx-background: #FFFFFF;");
        webView.getEngine().setUserStyleSheetLocation(getClass().getClassLoader().getResource("client/css/fx.css").toExternalForm());
        webView.getEngine().getLoadWorker().stateProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        ToolApplication.ElementHelper elh = new ToolApplication.ElementHelper(webView.getEngine().getDocument());//
                        final com.sun.webkit.dom.HTMLElementImpl trn_am = elh.getElementById("trn_am");
                        if (trn_am != null) {
                            elh.addEventListener("trn_am", "click", evt -> {
                                try {
                                    gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                                            Application.PropertySupport.Property.AM_PLAY,
                                            0, trn_am.getInnerText());
                                } catch (NumberFormatException ex) {
                                }
                            }, false);
                        }
                        final com.sun.webkit.dom.HTMLElementImpl trn_br = elh.getElementById("trn_am");
                        if (trn_br != null) {
                            elh.addEventListener("trn_br", "click", evt -> {
                                try {
                                    gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                                            Application.PropertySupport.Property.BR_PLAY,
                                            0, trn_br.getInnerText());
                                } catch (NumberFormatException ex) {
                                    //
                                }
                            }, false);
                        }
                        final com.sun.webkit.dom.HTMLElementImpl closeMenuItem = elh.getElementById("close_mi");
                        if (closeMenuItem != null) {
                            elh.addEventListener("close_mi", "click", evt -> {
                                contextMenu.hide();
                            }, false);
                        }
                        final com.sun.webkit.dom.HTMLElementImpl copyMenuItem = elh.getElementById("copy_mi");
                        if (copyMenuItem != null) {
                            elh.addEventListener("copy_mi", "click", evt -> {
                                try {
                                    gtranslator.Application.PROPERTYSUPPORT.firePropertyChange(
                                            Application.PropertySupport.Property.COPY_TO_CLIPBOARD,
                                            0, (webView.getEngine().documentProperty().get())
                                                    .getDocumentElement().getTextContent());
                                } catch (Exception ex) {
                                    logger.error(ex.getMessage(), ex);
                                }
                                contextMenu.hide();
                            }, false);
                        }
                    }
                });
        contextMenu = new ContextMenu() {
//            @Override
//            public void hide() {
//                Thread.dumpStack();
//                super.hide();
//            }
        };

        CustomMenuItem webMenuItem = new CustomMenuItem(webView);
        webMenuItem.getStyleClass().clear();
        webMenuItem.getStyleClass().add("menu-item1");
        webMenuItem.setHideOnClick(false);
        contextMenu.getItems().add(webMenuItem);
    }

    public void hide() {
        if (contextMenu.isShowing()) {
            contextMenu.hide();
        }
    }

    public void show(String textHtml) {
        show(textHtml, x, y);
    }

    public synchronized void show(String textHtml, int x, int y) {
        this.x = x;
        this.y = y;
        webView.getEngine().loadContent(textHtml, "text/html");
    }

    public boolean canHideWindow(int x, int y, int clickCount) {
        if (!contextMenu.isShowing()) {
            return false;
        }
        double h1 = contextMenu.getHeight();
        double w1 = contextMenu.getWidth();
        double x1 = contextMenu.getX();
        double y1 = contextMenu.getY();
        Rectangle rectangle = SwingUtilities.computeIntersection(x, y, 1, 1, new Rectangle((int) x1, (int) y1, (int) w1, (int) h1));
        return (clickCount == 1 && (rectangle.x | rectangle.y | rectangle.width | rectangle.height) == 0);
    }

    private double getSizeByJScript(boolean isHeight) {
        String script = "ts = document.getElementsByTagName('table');" +
                "v1 = ts.length == 0 ? 10 : ts[0].%1$s;" +
                "v2 = ts.length > 1 ? ts[1].%1$s : 10;" +
                "v1 > v2 ? v1 : v2";
        Object result = webView.getEngine().executeScript(String.format(script, isHeight ? "offsetHeight" : "offsetWidth"));
        if (result instanceof Number) {
            return ((Number) result).doubleValue();
        } else {
            return -1;
        }
    }

    private void adjustSizeAndShow(final Node anchNode) {
        Platform.runLater(() -> {
            try {
                double value = getSizeByJScript(false);
                if (value != -1) {
                    webView.setPrefWidth(value + 10);
                }
                value = getSizeByJScript(true);
                if (value != -1) {
                    webView.setPrefHeight(value + 10);
                }
                contextMenu.show(anchNode, x, y + OFFSET_DY);
            } catch (JSException ex) {
                ex.printStackTrace();
            }
        });
    }
}