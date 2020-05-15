package arbina.sps.api;

import arbina.infra.services.id.Authority;
import arbina.sps.BaseWebTest;
import arbina.sps.api.controller.TokensController;
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

@WebMvcTest(TokensController.class)
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
    @WithMockUser(authorities = Authority.OBSERVER)
    public void shouldBeOK() throws Exception {
        mvc.perform(get("/api"))
                .andExpect(status().isOk());
    }
}
