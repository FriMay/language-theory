package arbina.app.template.api.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import springfox.documentation.annotations.ApiIgnore;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private static Logger logger = LogManager.getLogger(CustomExceptionHandler.class);

    @ApiIgnore
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception ex, WebRequest request) throws Exception {

        logger.error("Exception during execution of application", ex);

        return handleException(ex, request);
    }
}