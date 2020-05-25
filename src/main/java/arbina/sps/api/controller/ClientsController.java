package arbina.sps.api.controller;


import arbina.infra.dto.AckDTO;
import arbina.infra.dto.CursoredListBodyDTO;
import arbina.infra.dto.CursoredListDTO;
import arbina.infra.exceptions.BadRequestException;
import arbina.infra.services.id.ArbinaId;
import arbina.infra.services.id.Authority;
import arbina.infra.services.id.dto.ClientDTO;
import arbina.infra.utils.DtoUtils;
import arbina.sps.api.dto.ApnsDTO;
import arbina.sps.api.dto.ClientConfigDTO;
import arbina.sps.api.dto.FcmDTO;
import arbina.sps.api.services.ClientsService;
import arbina.sps.config.SwaggerConfig;
import arbina.sps.store.entity.Apns;
import arbina.sps.store.entity.Client;
import arbina.sps.store.entity.Fcm;
import arbina.sps.store.repository.ClientsRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Controller
@Transactional
public class ClientsController implements DtoUtils {

    private final ArbinaId arbinaId;

    private final ClientsRepository clientsRepository;

    private final ClientsService clientsService;

    public ClientsController(ArbinaId arbinaId, ClientsRepository clientsRepository, ClientsService clientsService) {
        this.arbinaId = arbinaId;
        this.clientsRepository = clientsRepository;
        this.clientsService = clientsService;
    }

    @ApiOperation(value = "Fetch all clients id.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PostMapping("/api/settings/clients")
    @Secured({ Authority.PUSH_NOTIFIER })
    public ResponseEntity<CursoredListBodyDTO<ClientConfigDTO>> fetchClients(
            @RequestParam(defaultValue = "", required = false) String cursor,
            @RequestParam(defaultValue = "100", required = false) Integer limit,
            @RequestParam(required = false) String filter) {

        CursoredListBodyDTO<arbina.infra.services.id.dto.ClientDTO> clients =
                arbinaId.fetchClients(filter, cursor, limit);

        List<Client> clientList = new ArrayList<>();

        for (ClientDTO clientDto : clients.getItems()) {

            Client client = clientsRepository.findById(clientDto.getClientId()).orElse(null);

            if (client == null) {

                client = Client.builder()
                        .clientId(clientDto.getClientId())
                        .build();

                client = clientsRepository.saveAndFlush(client);
            }

            clientList.add(client);
        }

        Stream<Client> clientStream = clientList.stream();

        CursoredListDTO<Client, ClientConfigDTO> dto = new CursoredListDTO<>(clientStream.iterator(),
                cursor, limit, ClientConfigDTO::of, (long) clientList.size());

        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Fetch client by client id.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @GetMapping("/api/settings/clients/{client_id}")
    @Secured({ Authority.PUSH_NOTIFIER })
    public ResponseEntity<ClientConfigDTO> fetchClient(@PathVariable(name = "client_id") String clientId) {

        Client client = clientsService.validateAndGetClient(clientId);

        return ResponseEntity.ok(ClientConfigDTO.of(client));
    }

    @ApiOperation(value = "Patch configurable for client.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PatchMapping("/api/settings/clients/{client_id}")
    @Secured({ Authority.PUSH_NOTIFIER })
    public ResponseEntity<ClientConfigDTO> patchClient(
            @PathVariable(name = "client_id") String clientId,
            @RequestParam(name = "is_configurable") Boolean isConfigurable) {

        Client client = clientsService.validateAndGetClient(clientId);

        client.setIsConfigurable(isConfigurable);

        client = clientsRepository.saveAndFlush(client);

        return ResponseEntity.ok(ClientConfigDTO.of(client));
    }

    @ApiOperation(value = "Patch FCM for client.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PatchMapping("/api/settings/clients/{client_id}/fcm")
    @Secured({ Authority.PUSH_NOTIFIER })
    public ResponseEntity<FcmDTO> patchFcm(
            @PathVariable(name = "client_id") String clientId,
            @RequestParam(name = "configuration_file") MultipartFile configurationFile,
            @RequestParam String topic) {

        topic = Optional.ofNullable(topic).orElse("");

        if (topic.length() == 0) {
            throw new BadRequestException("Topic can't be empty.");
        }

        Client client = clientsService.validateAndGetClient(clientId);

        String stringConfig = validateFcmConfig(configurationFile);

        Fcm fcm = Fcm.builder()
                .updatedAt(new Date())
                .config(stringConfig)
                .build();

        client.setFcm(fcm);
        client.setIsConfigurable(true);
        client.setTopic(topic);

        client = clientsRepository.saveAndFlush(client);

        return ResponseEntity.ok(FcmDTO.of(client.getFcm()));
    }

    @ApiOperation(value = "Patch APNS for client.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PatchMapping("/api/settings/clients/{client_id}/apns")
    @Secured({ Authority.PUSH_NOTIFIER })
    public ResponseEntity<ApnsDTO> patchApns(
            @PathVariable(name = "client_id") String clientId,
            @RequestParam(name = "apns_certificate") MultipartFile apnsCeritificate,
            @RequestParam(name = "team_id") String teamId,
            @RequestParam(name = "key_id") String keyId,
            @RequestParam(name = "is_dev_gate") Boolean isDevGate,
            @RequestParam String topic
    ) {

        topic = Optional.ofNullable(topic).orElse("");

        if (topic.length() == 0) {
            throw new BadRequestException("Topic can't be empty.");
        }

        Client client = clientsService.validateAndGetClient(clientId);

        String stringConfig = validateApnsCertificate(apnsCeritificate);

        ApnsDTO apnsDTO = ApnsDTO.builder()
                .teamId(teamId)
                .keyId(keyId)
                .isDevGate(isDevGate)
                .build();

        validateObject(apnsDTO);

        Apns apns = Apns.of(apnsDTO);

        apns.setUpdatedAt(new Date());
        apns.setApnsCertificate(stringConfig);

        client.setApns(apns);
        client.setIsConfigurable(true);
        client.setTopic(topic);

        client = clientsRepository.saveAndFlush(client);

        return ResponseEntity.ok(ApnsDTO.of(client.getApns()));
    }

    @ApiOperation(value = "Delete client configuration.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @DeleteMapping("/api/settings/clients/{client_id}")
    @Secured({ Authority.PUSH_NOTIFIER })
    public ResponseEntity<AckDTO> deleteClientConfig(@PathVariable(name = "client_id") String clientId) {

        Client client = clientsService.validateAndGetClient(clientId);

        client.setApns(null);
        client.setFcm(null);
        client.setIsConfigurable(false);

        clientsRepository.saveAndFlush(client);

        return ResponseEntity.ok(new AckDTO(true));
    }

    /**
     * Validate and return FCM configuration as string.
     *
     * @param configurationFile FCM configuration file.
     * @return FCM configuration as string.
     */
    private String validateFcmConfig(MultipartFile configurationFile) {
        return validateAndGetConfigAsString(configurationFile);
    }

    /**
     * Validate and return APNS certificate as string.
     *
     * @param apnsCertificate APNS certificate
     * @return APNS certificate as string.
     */
    private String validateApnsCertificate(MultipartFile apnsCertificate) {
        return validateAndGetConfigAsString(apnsCertificate);
    }

    /**
     * Validating and return content of configuration file as string.
     *
     * @param configurationFile configuration file.
     * @return content of configuration file as string.
     */
    private String validateAndGetConfigAsString(MultipartFile configurationFile) {

        if (configurationFile == null) {
            throw new BadRequestException("The configuration file cannot be empty.");
        }

        try {

            String config = new String(configurationFile.getBytes());

            if (config.trim().length() == 0) {
                throw new BadRequestException("The content of configuration file cannot be empty");
            }

            return config;

        } catch (IOException e) {
            throw new BadRequestException("The configuration file cannot be read.");
        }
    }
}
