package gtranslator.client;

import gtranslator.Application;
import org.hibernate.mapping.Collection;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static java.lang.System.out;

@Ignore
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = {Application.class})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PopupWindowTest3 {
    {
        out.println(java.awt.GraphicsEnvironment.isHeadless());
    }

    @Autowired
    private Application application;


    @Test
    public void demoPopup() throws Exception {
        application.main(new String[0]);

        Thread.sleep(1000000);
    }
}
