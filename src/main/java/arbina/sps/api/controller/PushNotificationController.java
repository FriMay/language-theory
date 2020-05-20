package arbina.sps.api.controller;

import arbina.infra.dto.AckDTO;
import arbina.infra.exceptions.BadRequestException;
import arbina.infra.exceptions.NotFoundException;
import arbina.infra.services.id.Authority;
import arbina.sps.config.SwaggerConfig;
import arbina.sps.api.services.PushNotificationService;
import arbina.sps.store.entity.Template;
import arbina.sps.store.repository.TemplatesRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;

@Controller
@Transactional
public class PushNotificationController {

    private final TemplatesRepository templatesRepository;

    private final PushNotificationService pushNotificationService;

    public PushNotificationController(TemplatesRepository templatesRepository, PushNotificationService pushNotificationService) {
        this.templatesRepository = templatesRepository;
        this.pushNotificationService = pushNotificationService;
    }

    @ApiOperation(value = "Send notification by template name.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PostMapping("/templates/notifications")
    @Secured({Authority.PUSH_NOTIFIER})
    public ResponseEntity<AckDTO> sendDataNotification(
            @RequestParam(name = "template_name") String templateName
    ) {

        if (templateName == null) {
            throw new BadRequestException("Template id can't be empty");
        }

        Template template = templatesRepository.findByName(templateName).orElse(null);

        if (template == null) {
            throw new NotFoundException(String.format("Template with \"%s\" name is not found", templateName));
        }

        if (template.getLocalizations().isEmpty()){
            throw new NotFoundException(String.format("Template with \"%s\" name is not found", templateName));
        }

        pushNotificationService.sendPushNotification(template);

        return ResponseEntity.ok(new AckDTO());
    }

}

