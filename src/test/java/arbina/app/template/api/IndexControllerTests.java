package arbina.app.template.api;

import arbina.app.template.BaseWebTest;
import arbina.app.template.api.controller.IndexController;
import arbina.app.template.config.ResourceServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerTests extends BaseWebTest {

    @Autowired
    private WebApplicationContext webapp;

    private final Logger logger = LogManager.getLogger();

    private MockMvc mvc;

    @Before
    public void before() {
        mvc = super.before(this.webapp);
    }

    @Test
    @WithMockUser(authorities = ResourceServerConfig.OBSERVER)
    public void shouldBeOK() throws Exception {
        mvc.perform(get("/api"))
                .andExpect(status().isOk());
    }
}
