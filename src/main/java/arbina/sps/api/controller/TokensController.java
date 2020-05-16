package arbina.sps.api.controller;

import arbina.infra.dto.AckDTO;
import arbina.infra.exceptions.BadRequestException;
import arbina.infra.services.id.Authority;
import arbina.sps.config.SwaggerConfig;
import arbina.sps.store.DeviceTokenType;
import arbina.sps.store.entity.DeviceToken;
import arbina.sps.store.repository.DeviceTokenRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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

    public TokensController(DeviceTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @ApiOperation(value = "Put device token.",
            authorizations = { @Authorization(value = SwaggerConfig.oAuth2) })
    @PutMapping("/api/tokens")
    @Secured({ Authority.USER, Authority.OBSERVER })
    public ResponseEntity<AckDTO> putToken(@ApiIgnore Principal principal,
                                           String token,
                                           DeviceTokenType tokenType) {

        if (Optional.ofNullable(token).orElse("").length() == 0) {
            throw new BadRequestException("Wrong token");
        }

        boolean ack = false;

        if (!tokenRepository.isTokenExists(principal.getName(), token, tokenType.toString())) {

            DeviceToken deviceToken = DeviceToken.builder()
                    .username(principal.getName())
                    .token(token)
                    .tokenType(tokenType.toString())
                    .createdAt(new Date())
                    .build();

            tokenRepository.saveAndFlush(deviceToken);

            ack = true;
        }

        return ResponseEntity.ok(AckDTO.of(ack));
    }
}
