package arbina.sps.api.controller;

import arbina.infra.exceptions.BadRequestException;
import arbina.infra.exceptions.NotFoundException;
import arbina.infra.services.id.Authority;
import arbina.sps.api.dto.AnswerDTO;
import arbina.sps.api.services.ClientsService;
import arbina.sps.api.services.PushNotificationService;
import arbina.sps.config.SwaggerConfig;
import arbina.sps.store.entity.Client;
import arbina.sps.store.entity.Template;
import arbina.sps.store.repository.TemplatesRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Controller
@Transactional
public class PushNotificationController {

    private final TemplatesRepository templatesRepository;

    private final PushNotificationService pushNotificationService;

    private final ClientsService clientsService;

    private final ObjectMapper mapper;

    public PushNotificationController(
            TemplatesRepository templatesRepository,
            PushNotificationService pushNotificationService,
            ClientsService clientsService,
            ObjectMapper mapper) {

        this.templatesRepository = templatesRepository;
        this.pushNotificationService = pushNotificationService;
        this.clientsService = clientsService;
        this.mapper = mapper;
    }

    @ApiOperation(value = "Send notification by template name and client id.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PostMapping("/api/templates/notifications")
    @Secured({ Authority.PUSH_NOTIFIER })
    public ResponseEntity<List<AnswerDTO>> sendDataNotification(
            @RequestParam(name = "template_name") String templateName,
            @RequestParam(name = "client_ids") String clientIds) throws JsonProcessingException {

        if (templateName == null) {
            throw new BadRequestException("Template id can't be empty");
        }

        List<String> clientIdList = mapper.readValue(clientIds, new TypeReference<ArrayList<String>>() {});

        List<AnswerDTO> answers = new ArrayList<>();

        List<Thread> threads = new ArrayList<>();

        Template template = templatesRepository.findByName(templateName).orElse(null);

        if (template == null) {
            throw new NotFoundException(String.format("Template with \"%s\" name is not found", templateName));
        }

        if (template.getLocalizations().isEmpty()) {
            throw new NotFoundException("Add at least one localization to this template to use it");
        }

        clientIdList.forEach(clientId -> {

            Client client = clientsService.validateAndGetClient(clientId);

            if (client.getApns() == null && client.getFcm() == null) {

                answers.add(AnswerDTO.builder()
                        .result(false)
                        .reason(String.format("Add a configuration to client with \"%s\" " +
                                        "id for sending push notifications.",
                                client.getClientId()))
                        .build());

            } else {

                Thread thread = new Thread(new Thread(() -> {
                    pushNotificationService.sendPushNotification(template, client);
                }), client.getClientId());

                threads.add(thread);

                thread.start();
            }
        });

        threads.forEach(thread -> {

            try {

                thread.join();

                answers.add(AnswerDTO.builder()
                        .reason(String.format("Notifications to \"%s\" client id were sent successfully",
                                thread.getName()))
                        .result(true)
                        .build());
            } catch (InterruptedException e) {

                answers.add(AnswerDTO.builder()
                        .reason(String.format("An error occurred." +
                                        "Notifications to \"%s\" client id were not sent",
                                thread.getName()))
                        .result(false)
                        .build());
            }
        });

        return ResponseEntity.ok(answers);
    }
}

