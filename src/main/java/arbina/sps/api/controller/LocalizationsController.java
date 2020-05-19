package arbina.sps.api.controller;

import arbina.infra.dto.AckDTO;
import arbina.infra.dto.CursoredListBodyDTO;
import arbina.infra.dto.CursoredListDTO;
import arbina.infra.exceptions.BadRequestException;
import arbina.infra.exceptions.NotFoundException;
import arbina.infra.services.id.Authority;
import arbina.infra.utils.DtoUtils;
import arbina.sps.api.dto.LocalizationDTO;
import arbina.sps.config.SwaggerConfig;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.repository.LocalizationsRepository;
import arbina.sps.store.repository.TemplatesRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.apache.tomcat.jni.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Objects;
import java.util.stream.Stream;

@Controller
@Transactional
public class LocalizationsController implements DtoUtils {

    private final LocalizationsRepository localizationsRepository;

    private final TemplatesRepository templatesRepository;

    public LocalizationsController(LocalizationsRepository templateLocalizationRepository,
                                   TemplatesRepository templatesRepository) {
        this.localizationsRepository = templateLocalizationRepository;
        this.templatesRepository = templatesRepository;
    }

    @ApiOperation(value = "Fetch localization list for specified template.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @GetMapping("/api/templates/{templateId}/localizations")
    @Secured({
            Authority.OBSERVER,
            Authority.PUSH_MARKETING,
            Authority.PUSH_NOTIFIER
    })
    public ResponseEntity<CursoredListBodyDTO<LocalizationDTO>> fetchLocalizations(
            @PathVariable Long templateId,
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "100") Integer limit) {

        if (templateId == null) {
            throw new BadRequestException("Template id can't be empty");
        }

        Stream<Localization> localizationsStream = localizationsRepository.fetchAllByTemplateId(templateId);

        CursoredListDTO<Localization, LocalizationDTO> dto = new CursoredListDTO<>(localizationsStream.iterator(),
                        cursor, limit, LocalizationDTO::of);

        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Update a template localization.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PutMapping("/api/templates/localizations/{localizationId}")
    @Secured({Authority.PUSH_MARKETING})
    public ResponseEntity<LocalizationDTO> updateLocalization(@PathVariable Long localizationId,
                                                              @RequestParam String title,
                                                              @RequestParam String subtitle,
                                                              @RequestParam String body,
                                                              @RequestParam("locale_iso") String localeIso) {

        Localization localization = localizationsRepository.findById(localizationId).orElse(null);
        if (localization == null) {
            throw new BadRequestException("Locale is not exist.");
        }

        LocalizationDTO dto = LocalizationDTO.builder()
                .id(localizationId)
                .templateId(localization.getTemplateId())
                .title(title)
                .subtitle(subtitle)
                .body(body)
                .localeIso(localeIso)
                .build();

        validateObject(dto);

        Localization ent = localizationsRepository.findByTemplateIdAndLocale(dto.getTemplateId(), dto.getLocaleIso())
                .orElse(null);
        if (ent == null) {
            throw new BadRequestException("Template is not exist");
        } else if (!Objects.equals(ent.getId(), dto.getId())) {
            throw new BadRequestException("Template localization with this locale exists.");
        }

        Localization.fromDTO(dto, ent, templatesRepository);

        ent = localizationsRepository.saveAndFlush(ent);

        return ResponseEntity.ok(LocalizationDTO.of(ent));
    }

    @ApiOperation(value = "Create a template localization.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PostMapping("/api/templates/{templateId}/localizations")
    @Secured({Authority.PUSH_MARKETING})
    public ResponseEntity<LocalizationDTO> createLocalization(@PathVariable Long templateId,
                                                              @RequestParam String title,
                                                              @RequestParam String subtitle,
                                                              @RequestParam String body,
                                                              @RequestParam("locale_iso") String localeIso) {

        LocalizationDTO dto = LocalizationDTO.builder()
                .templateId(templateId)
                .title(title)
                .subtitle(subtitle)
                .body(body)
                .localeIso(localeIso)
                .build();

        Localization localization = localizationsRepository
                .findByTemplateIdAndLocale(dto.getTemplateId(), dto.getLocaleIso()).orElse(null);

        if (localization != null) {
            throw new BadRequestException("Template locale with this localization exists.");
        }

        Localization ent = new Localization();

        Localization.fromDTO(dto, ent, templatesRepository);

        ent = localizationsRepository.saveAndFlush(ent);

        return ResponseEntity.ok(LocalizationDTO.of(ent));
    }

    @ApiOperation(value = "Delete a template localization.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @DeleteMapping("/api/templates/localizations/{localizationId}")
    @Secured({Authority.PUSH_MARKETING})
    public ResponseEntity<AckDTO> deleteLocalization(@PathVariable Long localizationId) {

        if (!localizationsRepository.existsById(localizationId)) {
            throw new NotFoundException(String.format("Template localization \"%s\" is not exist", localizationId));
        }

        localizationsRepository.deleteById(localizationId);

        return ResponseEntity.ok(new AckDTO());
    }

}
