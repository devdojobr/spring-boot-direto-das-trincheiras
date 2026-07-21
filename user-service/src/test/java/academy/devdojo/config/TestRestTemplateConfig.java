package academy.devdojo.config;

import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static academy.devdojo.commons.Constants.*;

@TestConfiguration
@Lazy
public class TestRestTemplateConfig {
    @LocalServerPort
    int port;

    @Bean
    public TestRestTemplate testRestTemplate() {
        var uri = new DefaultUriBuilderFactory(BASE_URI + port);
        var testRestTemplate = new TestRestTemplate()
                .withBasicAuth(REGULAR_USERNAME, PASSWORD);
        testRestTemplate.setUriTemplateHandler(uri);

        return testRestTemplate;
    }
}
