package gtranslator;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ApplicationConfig {
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Autowired
    private ConfigurableEnvironment env;

    /*
    @PostConstruct
    public void postConstruct() {
        setDefaultPropertySource("custom.prop.src.lang", "en");
        setDefaultPropertySource("custom.prop.trg.lang", "ru");
        setDefaultPropertySource("custom.prop.amount_view_words", "10");
    }

    private void setDefaultPropertySource(String propName, Object value) {
        if (StringUtils.isBlank(env.getProperty(propName))) {
            env.getPropertySources()
                    .addFirst(new MapPropertySource(propName,
                            ImmutableMap.of(propName, value)));
        }
    }
*/
}
