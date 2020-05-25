package arbina.sps.api;

import arbina.infra.services.id.Authority;
import arbina.sps.BaseWebTest;
import arbina.sps.api.controller.TemplatesController;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.entity.Template;
import arbina.sps.store.repository.LocalizationsRepository;
import arbina.sps.store.repository.TemplatesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TemplatesController.class)
public class TemplatesTest extends BaseWebTest {

    private static boolean isDataSetup = false;

    private static Long firstTemplateId;

    private static Long secondTemplateId;

    @Autowired
    private WebApplicationContext webapp;

    private final Logger logger = LogManager.getLogger();

    @Autowired
    private LocalizationsRepository localizationsRepository;

    @Autowired
    private TemplatesRepository templatesRepository;

    private MockMvc mvc;

    private void initData() {

        Map<String, String> params = new HashMap<>();

        params.put("hello", "World");

        Template firstTemplate = Template.builder()
                .name("For administrator")
                .description("Useful description.")
                .params(params)
                .build();

        Template secondTemplate = Template.builder()
                .name("I will remove.")
                .description("Remove me.")
                .params(params)
                .build();

        firstTemplate = templatesRepository.saveAndFlush(firstTemplate);

        firstTemplateId = firstTemplate.getId();

        secondTemplateId = templatesRepository.saveAndFlush(secondTemplate).getId();

        Localization firstLocalization = Localization.builder()
                .title("Hello Michael!")
                .subtitle("Hello message.")
                .body("Our app has been updated, come soon!")
                .template(firstTemplate)
                .localeIso("ru")
                .build();

        Localization secondLocalization = Localization.builder()
                .title("Hello Michael!")
                .subtitle("Hello message.")
                .body("Our app has been updated, come soon!")
                .template(firstTemplate)
                .localeIso("en")
                .build();

        localizationsRepository.saveAndFlush(firstLocalization);
        localizationsRepository.saveAndFlush(secondLocalization);
    }

    @Before
    public void before() {
        mvc = super.before(this.webapp);

        if (!isDataSetup) {
            isDataSetup = true;
            initData();
        }
    }

    @Test
    @WithMockUser(authorities = Authority.OBSERVER)
    public void shouldReturnNonEmptyList() throws Exception {

        mvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = Authority.OBSERVER)
    public void shouldReturnFilteredByNameList() throws Exception {

        mvc.perform(get("/api/templates")
                .param("filter", "or"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = Authority.PUSH_MARKETING)
    public void shouldReturnUpdatedTemplate() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("Change", "Data");
        params.put("Hello", "Dmitriy");

        mvc.perform(put("/api/templates/{templateId}", firstTemplateId)
                .param("name", "Changed template")
                .param("description", "hello world")
                .param("badge", "3")
                .param("params_json", new ObjectMapper().writeValueAsString(params))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = Authority.PUSH_MARKETING)
    public void shouldReturnCreatedTemplate() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("Change", "Data");
        params.put("Hello", "Dmitriy");

        mvc.perform(post("/api/templates")
                .param("name", "qwe")
                .param("description", "hello world")
                .param("badge", "3")
                .param("params_json", new ObjectMapper().writeValueAsString(params))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = Authority.PUSH_MARKETING)
    public void shouldDelete() throws Exception {
        mvc.perform(delete("/api/templates/{templateId}", secondTemplateId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
