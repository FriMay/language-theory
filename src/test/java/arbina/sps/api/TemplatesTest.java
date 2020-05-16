package arbina.sps.api;

import arbina.infra.services.id.Authority;
import arbina.sps.BaseWebTest;
import arbina.sps.api.controller.TemplatesController;
import arbina.sps.store.entity.Template;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.repository.LocalizationsRepository;
import arbina.sps.store.repository.TemplatesRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

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
    private LocalizationsRepository templateLocalizationRepository;

    @Autowired
    private TemplatesRepository templatesRepository;

    private MockMvc mvc;

    private void initData() {

        Template firstTemplate = Template.builder()
                .name("For administrator")
                .description("Useful description.")
                .build();

        Template secondTemplate = Template.builder()
                .name("I will remove.")
                .description("Remove me.")
                .build();

        firstTemplate = templatesRepository.saveAndFlush(firstTemplate);
        firstTemplateId = firstTemplate.getId();

        secondTemplateId = templatesRepository.saveAndFlush(secondTemplate).getId();

        Localization firstTemplateLocalization = Localization.builder()
                .title("Hello Michael!")
                .subtitle("Hello message.")
                .body("Our app has been updated, come soon!")
                .template(firstTemplate)
                .localeIso("ru")
                .build();

        Localization secondTemplateLocalization = Localization.builder()
                .title("Hello Michael!")
                .subtitle("Hello message.")
                .body("Our app has been updated, come soon!")
                .template(firstTemplate)
                .localeIso("en")
                .build();

        templateLocalizationRepository.saveAndFlush(firstTemplateLocalization);
        templateLocalizationRepository.saveAndFlush(secondTemplateLocalization);
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
    @WithMockUser(authorities = Authority.EMAIL_MARKETING)
    public void shouldReturnNonEmptyList() throws Exception {
        mvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = Authority.EMAIL_MARKETING)
    public void shouldReturnFilteredByNameList() throws Exception {
        mvc.perform(get("/api/templates")
                .param("filter", "or"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = Authority.EMAIL_MARKETING)
    public void shouldReturnUpdatedTemplate() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "Changed template");
        jsonObject.put("description", "hello world");

        mvc.perform(put("/api/templates/{templateId}", firstTemplateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = Authority.EMAIL_MARKETING)
    public void shouldDelete() throws Exception {
        mvc.perform(delete("/api/templates/{templateId}", secondTemplateId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
