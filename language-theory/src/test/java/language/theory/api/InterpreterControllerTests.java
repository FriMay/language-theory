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

@WebMvcTest(InterpreterController.class)
public class InterpreterControllerTests extends BaseWebTest {

    @Autowired
    private WebApplicationContext webapp;

    @Autowired
    private InterpreterController controller;

    private final Logger logger = LogManager.getLogger();

    private MockMvc mvc;

    private static boolean isDataSetup = false;

    private void initData() { }

    @Before
    public void before() {

        mvc = super.before(this.webapp);

        if (!isDataSetup) {

            initData();

            isDataSetup = true;
        }
    }

    @Test
    public void shouldBeOK() {

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

    }
}
