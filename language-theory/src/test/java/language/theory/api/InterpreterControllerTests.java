package language.theory.api;

import language.theory.BaseWebTest;
import language.theory.api.controller.InterpreterController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
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
    public void shouldBeOK() throws Exception {

        String testProgram = ("var a,b: integer;" +
                "begin" +
                "read(a);" +
                "read(b);" +
                "write(a);" +
                "write(b);" +
                "b = (a + 21)*2;" +
                "write(b);" +
                "a = (a + b) * (10+5);" +
                "write(a);" +
                "end").replaceAll("\\s+", "");

        ResponseEntity entity = controller.interpretProgram(testProgram, "5 7");

        System.out.println(entity.getBody());

    }
}
