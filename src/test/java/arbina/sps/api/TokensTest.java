package arbina.sps.api;

import arbina.infra.services.id.Authority;
import arbina.sps.BaseWebTest;
import arbina.sps.api.controller.TokensController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TokensController.class)
public class TokensTest extends BaseWebTest {

    @Autowired
    private WebApplicationContext webapp;

    private MockMvc mvc;

    @Before
    public void before() {
        mvc = super.before(this.webapp);
    }

    @Test
    @WithMockUser(authorities = Authority.USER)
    public void shouldReturnUpdatedTemplate() throws Exception {

        mvc.perform(put("/api/tokens")
                .param("token", "spat")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
