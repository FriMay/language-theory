package language.theory.api;

import language.theory.BaseWebTest;
import language.theory.api.controller.InterpreterController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterpreterController.class)
public class InterpreterControllerTests extends BaseWebTest {

    @Autowired
    private WebApplicationContext webapp;

    private final Logger logger = LogManager.getLogger();

    private MockMvc mvc;

    private static boolean isDataSetup = false;

    private void initData() {
    }

    @Before
    public void before() {

        mvc = super.before(this.webapp);

        if (!isDataSetup) {

            initData();

            isDataSetup = true;
        }
    }

    @Test
    public void shouldBeOK() throws Exception {

        String testProgram = ("var i, j: integer;" +
                "begin" +
                "for (i = 2) to 5 do" +
                "for (i = 5) to 6 do" +
                "end_for;" +
                "for (j = 5) to 6 do" +
                "write(j);" +
                "end_for;" +
                "write(i);" +
                "end_for;" +
                "end").replaceAll("\\s+", "");

        mvc.perform(
                get("/interpret")
                        .param("program", testProgram)
                        .param("params", "1 2"))
                .andExpect(status().isOk());
    }
}
