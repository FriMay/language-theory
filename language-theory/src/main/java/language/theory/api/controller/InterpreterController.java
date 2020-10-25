package language.theory.api.controller;

import language.theory.interpreter.Interpreter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class InterpreterController {

    @GetMapping(value = "/interpret")
    public void interpretProgram(@RequestParam(required = false) String program, @RequestParam(required = false) String params, HttpServletResponse response) throws IOException {

        response.setStatus(200);

        Interpreter interpreter = new Interpreter();

        try {
            response.getWriter().println(interpreter.computeEnteredProgram(program, params));
        } catch (Exception e) {
            response.getWriter().println(e.getMessage());
        }
    }
}
