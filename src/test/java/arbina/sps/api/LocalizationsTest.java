package arbina.sps.api;


import arbina.infra.services.id.Authority;
import arbina.sps.BaseWebTest;
import arbina.sps.api.controller.LocalizationsController;
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
    @WithMockUser(authorities = Authority.EMAIL_MARKETING)
    public void shouldReturnNonEmptyList() throws Exception {
        mvc.perform(get("/api/templates/localizations/{templateId}", firstTemplateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = Authority.EMAIL_MARKETING)
    public void shouldReturnCreatedTemplateLocalization() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Some subject");
        jsonObject.put("subtitle", "Do you like {{ cookie_name }}?");
        jsonObject.put("body", "<h1>Do you like {{ cookie_name }}?</h1>");
        jsonObject.put("template_id", firstTemplateId);
        jsonObject.put("locale_iso", "eu");

        mvc.perform(post("/api/templates/localizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = Authority.EMAIL_MARKETING)
    public void shouldReturnBadRequestOnCreate() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Bad template");
        jsonObject.put("subtitle", "Do you like a {{ bad_request }}?");
        jsonObject.put("body", "<h1>Do you like a {{ bad_request }}?</h1>");
        jsonObject.put("template_id", firstTemplateId);
        jsonObject.put("locale_iso", "eu");

        mvc.perform(post("/api/templates/localizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = Authority.EMAIL_MARKETING)
    public void shouldReturnUpdatedTemplateLocalization() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "New subject");
        jsonObject.put("subtitle", "How about {{ lemonade_name }}?");
        jsonObject.put("body", "<h1>How about {{ lemonade_name }}?</h1>");
        jsonObject.put("template_id", firstTemplateId);
        jsonObject.put("locale_iso", "ru");

        mvc.perform(put("/api/templates/localizations/{localizationId}", firstLocalizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = Authority.EMAIL_MARKETING)
    public void shouldReturnBadRequestOnUpdate() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Update subject");
        jsonObject.put("subtitle", "How about {{ bad_request }}?");
        jsonObject.put("body", "<h1>How about {{ bad_request }}?</h1>");
        jsonObject.put("template_id", firstTemplateId);
        jsonObject.put("locale_iso", "eu");

        mvc.perform(put("/api/templates/localizations/{localizationId}", firstLocalizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString()))
                .andExpect(status().isBadRequest());
    }

}