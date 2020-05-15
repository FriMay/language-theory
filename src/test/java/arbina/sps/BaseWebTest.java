package arbina.sps;

import arbina.sps.config.CorsConfig;
import arbina.sps.config.ResourceServerConfig;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Import({
        BaseWebContext.class,
        ResourceServerConfig.class,
        CorsConfig.class
})
public class BaseWebTest {

    public MockMvc before(WebApplicationContext webapp) {

        return MockMvcBuilders.webAppContextSetup(webapp)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }
}