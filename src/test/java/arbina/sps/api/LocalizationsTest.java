package arbina.sps.api;

import arbina.infra.services.id.Authority;
import arbina.sps.BaseWebTest;
import arbina.sps.api.controller.LocalizationsController;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.entity.Template;
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

@WebMvcTest(LocalizationsController.class)
public class LocalizationsTest extends BaseWebTest {

    private static boolean isDataSetup = false;

    private static Long firstLocalizationId;

    private static Long firstTemplateId;

    private final Logger logger = LogManager.getLogger();

    @Autowired
    private LocalizationsRepository localizationsRepository;

    @Autowired
    private TemplatesRepository templatesRepository;

    @Autowired
    private WebApplicationContext webapp;

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

        templatesRepository.saveAndFlush(secondTemplate);

        Localization firstLocalization = Localization.builder()
                .title("Hello Michael!")
                .subtitle("Hello message.")
                .body("Our app has been updated, come soon!")
                .template(firstTemplate)
                .localeIso("ru")
                .build();

        Localization secondeLocalization = Localization.builder()
                .title("Hello Michael!")
                .subtitle("Hello message.")
                .body("Our app has been updated, come soon!")
                .template(firstTemplate)
                .localeIso("en")
                .build();

        firstLocalizationId = localizationsRepository.saveAndFlush(firstLocalization).getId();
        localizationsRepository.saveAndFlush(secondeLocalization);
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

        mvc.perform(get("/api/templates/{template_id}/localizations", firstTemplateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = Authority.PUSH_MARKETING)
    public void shouldReturnCreatedTemplateLocalization() throws Exception {

        mvc.perform(post("/api/templates/{template_id}/localizations", firstTemplateId)
                .param("title", "Some subject")
//                .param("subtitle", "Do you like {{ cookie_name }}?")
//                .param("body", "<h1>Do you like {{ cookie_name }}?</h1>")
                .param("locale_iso", "eu")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = Authority.PUSH_MARKETING)
    public void shouldReturnBadRequestOnCreate() throws Exception {

        mvc.perform(post("/api/templates/{template_id}/localizations", firstTemplateId)
                .param("title", "Bad template")
                .param("subtitle", "Do you like a {{ bad_request }}?")
                .param("body", "<h1>Do you like a {{ bad_request }}?</h1>")
                .param("locale_iso", "eu")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = Authority.PUSH_MARKETING)
    public void shouldReturnUpdatedTemplateLocalization() throws Exception {

        mvc.perform(put("/api/templates/localizations/{localization_id}", firstLocalizationId)
                .param("title", "New subject")
                .param("subtitle", "How about {{ lemonade_name }}?")
                .param("body", "<h1>How about {{ lemonade_name }}?</h1>")
                .param("template_id", String.valueOf(firstTemplateId))
                .param("locale_iso", "ru")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = Authority.PUSH_MARKETING)
    public void shouldReturnBadRequestOnUpdate() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Update subject");
        jsonObject.put("subtitle", "How about {{ bad_request }}?");
        jsonObject.put("body", "<h1>How about {{ bad_request }}?</h1>");
        jsonObject.put("template_id", firstTemplateId);
        jsonObject.put("locale_iso", "eu");

        mvc.perform(put("/api/templates/localizations/{localization_id}", firstLocalizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString()))
                .andExpect(status().isBadRequest());
    }
}
