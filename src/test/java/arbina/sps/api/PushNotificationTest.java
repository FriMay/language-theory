package arbina.sps.api;

import arbina.infra.services.id.Authority;
import arbina.sps.BaseWebTest;
import arbina.sps.api.controller.PushNotificationController;
import arbina.sps.store.DeviceTokenType;
import arbina.sps.store.entity.DeviceToken;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.entity.Template;
import arbina.sps.store.repository.DeviceTokenRepository;
import arbina.sps.store.repository.LocalizationsRepository;
import arbina.sps.store.repository.TemplatesRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PushNotificationController.class)
public class PushNotificationTest extends BaseWebTest {

    private static boolean isDataSetup = false;

    @Autowired
    private WebApplicationContext webapp;

    @Autowired
    private LocalizationsRepository localizationsRepository;

    @Autowired
    private TemplatesRepository templatesRepository;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    private final Logger logger = LogManager.getLogger();

    private MockMvc mvc;

    private void initData() {

        Map<String, String> params = new HashMap<>();

        params.put("hello", "World");

        Template firstTemplate = Template.builder()
                .name("For administrator")
                .description("Useful description.")
                .badge(4)
                .params(params)
                .build();

        firstTemplate = templatesRepository.saveAndFlush(firstTemplate);

        Localization firstLocalization = Localization.builder()
                .title("Привет Алесей!")
                .subtitle("Ку-ку!")
                .body("Данное сообщение должно быть отправлено на русском языке!")
                .template(firstTemplate)
                .localeIso("ru")
                .build();

        Localization secondLocalization = Localization.builder()
                .title("Hello Alexey!")
                .subtitle("Ky-ky!")
                .body("This message should be push on English language!")
                .template(firstTemplate)
                .localeIso("en")
                .build();

        localizationsRepository.saveAndFlush(firstLocalization);

        localizationsRepository.saveAndFlush(secondLocalization);

        DeviceToken firstToken = DeviceToken.builder()
                .token("dsycLQKqRN6r3L-QwenTDx:APA91bFtToX6cyxZWQY1Pi7DCArmvzO1vmj1HgoZ4l7FYo0VDEysvrLIDvaiAvfUy3Yoc0OBupLsZJGvdSDsxfdQOgmmHZBRzCTsPLa71qHxnA-7YmStM9-E2sT_VLCVbNK9HVfXUm1q")
                .tokenType(DeviceTokenType.FCM.name())
                .localeIso("ru")
                .username("alexey")
                .build();

        deviceTokenRepository.saveAndFlush(firstToken);

        DeviceToken secondToken = DeviceToken.builder()
                .token("f8020b06be0d4df3ea1fabb75dbcae663a7625b52618753421ca212db149ff7d")
                .tokenType(DeviceTokenType.IOS.name())
                .localeIso("en")
                .username("alexey_ios")
                .build();

        deviceTokenRepository.saveAndFlush(secondToken);

        DeviceToken thirdToken = DeviceToken.builder()
                .token("feQcqb0tS_6Pswhll4FyFi:APA91bG5hN_ONRMirzK9qeGrP1H6MMNQ9b4HffRk0q7Q5s7kKnw4N3eVS9WxdZ97nlo-G5ovCbbjnJZatK8fpWyIZIlfFC6hMcyJ-Lv90VG84_5SxTgm5ha1JSYEDsCvzexBo_zyjbdS")
                .tokenType(DeviceTokenType.FCM.name())
                .localeIso("en")
                .username("vlad")
                .build();

        deviceTokenRepository.saveAndFlush(thirdToken);
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
    @WithMockUser(authorities = Authority.PUSH_NOTIFIER)
    public void shouldReturnOk() throws Exception {
        mvc.perform(post("/templates/notifications")
                .param("template_name", "For administrator"))
                .andExpect(status().isOk());
    }

}
