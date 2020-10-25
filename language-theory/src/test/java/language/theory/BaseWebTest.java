package language.theory;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
public class BaseWebTest {

    public MockMvc before(WebApplicationContext webapp) {

        return MockMvcBuilders.webAppContextSetup(webapp)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }
}