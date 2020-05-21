package arbina.sps.api.controller;


import arbina.infra.dto.AckDTO;
import arbina.infra.dto.CursoredListBodyDTO;
import arbina.infra.dto.CursoredListDTO;
import arbina.infra.exceptions.BadRequestException;
import arbina.infra.services.id.ArbinaId;
import arbina.infra.services.id.Authority;
import arbina.infra.utils.DtoUtils;
import arbina.sps.api.dto.ApnsDTO;
import arbina.sps.api.dto.ClientDTO;
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
    @Secured({Authority.PUSH_NOTIFIER})
    public ResponseEntity<CursoredListBodyDTO<ClientDTO>> fetchClients(
            @RequestParam(defaultValue = "", required = false) String cursor,
            @RequestParam(defaultValue = "100", required = false) Integer limit,
            @RequestParam(required = false) String filter) {

        CursoredListBodyDTO<arbina.infra.services.id.dto.ClientDTO> clients =
                arbinaId.fetchClients(filter, cursor, limit);

        List<Client> clientList = new ArrayList<>();

        for (arbina.infra.services.id.dto.ClientDTO clientDto: clients.items){

            Client client = clientsRepository.findById(clientDto.clientId).orElse(null);

            if (client == null){

                client = Client.builder()
                        .clientId(clientDto.clientId)
                        .build();

                client = clientsRepository.saveAndFlush(client);
            }

            clientList.add(client);

        }

        Stream<Client> clientStream = clientList.stream();

        CursoredListDTO<Client, ClientDTO> dto = new CursoredListDTO<>(clientStream.iterator(),
                cursor, limit, ClientDTO::of, clientStream.count());

        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Fetch client by client id.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @GetMapping("/api/settings/clients/{clientId}")
    @Secured({Authority.PUSH_NOTIFIER})
    public ResponseEntity<ClientDTO> fetchClient(@PathVariable String clientId) {

        Client client = clientsService.validateAndGetClient(clientId);

        return ResponseEntity.ok(ClientDTO.of(client));
    }

    @ApiOperation(value = "Patch configurable for client.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PatchMapping("/api/settings/clients/{clientId}")
    @Secured({Authority.PUSH_NOTIFIER})
    public ResponseEntity<ClientDTO> patchClient(
            @PathVariable String clientId,
            @RequestParam(name = "is_configurable") Boolean isConfigurable) {

        if (isConfigurable == null){
            throw new BadRequestException("Configurable can't be empty.");
        }

        Client client = clientsService.validateAndGetClient(clientId);

        client.setIsConfigurable(isConfigurable);

        client = clientsRepository.saveAndFlush(client);

        return ResponseEntity.ok(ClientDTO.of(client));
    }

    @ApiOperation(value = "Patch FCM for client.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PatchMapping("/api/settings/clients/{clientId}/fcm")
    @Secured({Authority.PUSH_NOTIFIER})
    public ResponseEntity<FcmDTO> patchFcm(
            @PathVariable String clientId,
            @RequestParam MultipartFile configuration) {

        Client client = clientsService.validateAndGetClient(clientId);

        String stringConfig = validateAndGetConfigAsString(configuration);

        Fcm fcm = Fcm.builder()
                .updatedAt(new Date())
                .config(stringConfig)
                .build();

        client.setFcm(fcm);
        client.setIsConfigurable(true);

        client = clientsRepository.saveAndFlush(client);

        return ResponseEntity.ok(FcmDTO.of(client.getFcm()));
    }

    @ApiOperation(value = "Patch APNS for client.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PatchMapping("/api/settings/clients/{clientId}/apns")
    @Secured({Authority.PUSH_NOTIFIER})
    public ResponseEntity<ApnsDTO> patchApns(
            @PathVariable String clientId,
            @RequestParam MultipartFile configuration,
            @RequestParam(name = "team_id") String teamId,
            @RequestParam(name = "key_id") String keyId,
            @RequestParam(name = "is_dev_gate") Boolean isDevGate
    ) {

        Client client = clientsService.validateAndGetClient(clientId);

        String stringConfig = validateAndGetConfigAsString(configuration);

        ApnsDTO apnsDTO = ApnsDTO.builder()
                .teamId(teamId)
                .keyId(keyId)
                .isDevGate(isDevGate)
                .build();

        validateObject(apnsDTO);

        Apns apns = Apns.of(apnsDTO);

        apns.setUpdatedAt(new Date());
        apns.setConfig(stringConfig);

        client.setApns(apns);
        client.setIsConfigurable(true);

        client = clientsRepository.saveAndFlush(client);

        return ResponseEntity.ok(ApnsDTO.of(client.getApns()));
    }

    @ApiOperation(value = "Delete client configuration.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @DeleteMapping("/api/settings/clients/{clientId}")
    @Secured({Authority.PUSH_NOTIFIER})
    public ResponseEntity<AckDTO> deleteClientConfig(@PathVariable String clientId) {

        Client client = clientsService.validateAndGetClient(clientId);

        client.setApns(null);
        client.setFcm(null);
        client.setIsConfigurable(false);

        clientsRepository.saveAndFlush(client);

        return ResponseEntity.ok(new AckDTO());
    }

    /**
     * Validating and return content of configuration file as string.
     *
     * @param configuration configuration file.
     * @return content of configuration file as string.
     */
    private String validateAndGetConfigAsString(MultipartFile configuration) {

        if (configuration == null) {
            throw new BadRequestException("The configuration file cannot be empty.");
        }

        try {

            String config = new String(configuration.getBytes());

            if (config.trim().length() == 0) {
                throw new BadRequestException("The content of configuration file cannot be empty");
            }

            return config;

        } catch (IOException e) {
            throw new BadRequestException("The configuration file cannot be read.");
        }
    }

}
