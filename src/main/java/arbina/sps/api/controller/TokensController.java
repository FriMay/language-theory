package arbina.sps.api.controller;

import arbina.infra.dto.AckDTO;
import arbina.infra.exceptions.BadRequestException;
import arbina.infra.services.id.Authority;
import arbina.sps.api.services.ClientsService;
import arbina.sps.config.SwaggerConfig;
import arbina.sps.store.entity.Client;
import arbina.sps.store.entity.DeviceToken;
import arbina.sps.store.repository.DeviceTokenRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import springfox.documentation.annotations.ApiIgnore;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.Date;
import java.util.Optional;

@Controller
@Transactional
public class TokensController {

    private final DeviceTokenRepository tokenRepository;

    private final ClientsService clientsService;

    public TokensController(DeviceTokenRepository tokenRepository, ClientsService clientsService) {
        this.tokenRepository = tokenRepository;
        this.clientsService = clientsService;
    }

    @ApiOperation(value = "Put device token.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PutMapping("/api/tokens")
    @Secured({
            Authority.USER,
            Authority.OBSERVER
    })
    public ResponseEntity<AckDTO> putToken(@ApiIgnore Principal principal,
                                           @ApiIgnore OAuth2Authentication auth,
                                           String token,
                                           String localIso) {

        String clientId = auth.getOAuth2Request().getClientId();

        Client client = clientsService.validateAndGetClient(clientId);

        if (Optional.ofNullable(token).orElse("").length() == 0) {
            throw new BadRequestException("Wrong token");
        }

        if (Optional.ofNullable(localIso).orElse("").length() == 0) {
            throw new BadRequestException("Wrong locale iso");
        }

        boolean ack = false;

        if (!tokenRepository.isTokenExists(principal.getName(), token, client.getClientId())) {

            DeviceToken deviceToken = DeviceToken.builder()
                    .username(principal.getName())
                    .token(token)
                    .localeIso(localIso)
                    .client(client)
                    .createdAt(new Date())
                    .build();

            tokenRepository.saveAndFlush(deviceToken);

            ack = true;
        }

        return ResponseEntity.ok(AckDTO.of(ack));
    }
}
