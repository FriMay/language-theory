package arbina.sps.api;

import arbina.infra.services.id.Authority;
import arbina.sps.BaseWebTest;
import arbina.sps.api.controller.ClientsController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientsController.class)
public class ClientsTest extends BaseWebTest {

    @Autowired
    private WebApplicationContext webapp;

    private MockMvc mvc;

    @Before
    public void before() {
        mvc = super.before(this.webapp);
    }

    @Test
    @WithMockUser(authorities = Authority.PUSH_NOTIFIER)
    public void shouldReturnExpectationFailed() throws Exception {

        mvc.perform(get("/api/settings/clients/{client_id}", "random_client_id"))
                .andExpect(status().isNotFound());
    }
}

