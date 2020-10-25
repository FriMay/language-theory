package language.theory.api.controller;

import language.theory.interpreter.Interpreter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;

@Controller
@Transactional
public class InterpreterController {

    @GetMapping("/api/interpret")
    public ResponseEntity<String> interpretProgram(@RequestParam String program, @RequestParam String params) {
        try {
            return ResponseEntity.ok(Interpreter.computeEnteredProgram(program, params));
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body(e.getMessage());
        }
    }
}
