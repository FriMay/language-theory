package arbina.sps.api.controller;

import arbina.infra.dto.AckDTO;
import arbina.infra.exceptions.BadRequestException;
import arbina.infra.exceptions.NotFoundException;
import arbina.sps.firebase.service.PushNotificationService;
import arbina.sps.store.entity.Template;
import arbina.sps.store.repository.TemplatesRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.transaction.Transactional;

@Controller
@Transactional
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    private final TemplatesRepository templatesRepository;

    public PushNotificationController(PushNotificationService pushNotificationService,
                                      TemplatesRepository templatesRepository) {

        this.pushNotificationService = pushNotificationService;
        this.templatesRepository = templatesRepository;
    }

    @PostMapping("/templates/notifications/{templateId}")
    public ResponseEntity<AckDTO> sendDataNotification(@PathVariable Long templateId) {

        if (templateId == null) {
            throw new BadRequestException("Template id can't be empty");
        }

        Template template = templatesRepository.findById(templateId).orElse(null);

        if (template == null) {
            throw new NotFoundException(String.format("Template #%s is not found", templateId));
        }

        pushNotificationService.sendPushNotification(template);

        return ResponseEntity.ok(new AckDTO());
    }

}
