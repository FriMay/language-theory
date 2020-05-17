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
import arbina.sps.store.entity.Template;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.repository.LocalizationsRepository;
import arbina.sps.store.repository.TemplatesRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Optional;
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
    @GetMapping("/api/templates/localizations/{templateId}")
    @Secured({
            Authority.OBSERVER,
            Authority.PUSH_MARKETING,
            Authority.PUSH_NOTIFIER
    })
    public ResponseEntity<CursoredListBodyDTO<LocalizationDTO>> fetchLocalizations(
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "100") Integer limit,
            @PathVariable Long templateId) {

        if (templateId == null) {
            throw new BadRequestException("Template id can't be empty");
        }

        Stream<Localization> localizationsStream = localizationsRepository.fetchAllByTemplateId(templateId);

        CursoredListDTO<Localization, LocalizationDTO> dto =
                new CursoredListDTO<>(localizationsStream.iterator(),
                        cursor, limit, LocalizationDTO::of);

        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Update a template localization.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PutMapping("/api/templates/localizations/{localizationId}")
    @Secured({Authority.PUSH_MARKETING})
    public ResponseEntity<LocalizationDTO> updateTemplateLocalization(@PathVariable Long localizationId,
                                                                      @RequestBody LocalizationDTO dto) {

        if (localizationId == null) {
            throw new BadRequestException("Template localization id can't be empty");
        }

        validateObject(dto);

        Localization localization = localizationsRepository
                        .findByTemplateIdAndLocale(dto.getTemplateId(), dto.getLocaleIso())
                        .orElse(null);

        if (localization != null && !localizationId.equals(localization.getId())) {
            throw new BadRequestException("Template localization with this locale exists, choice another locale.");
        }

        Localization ent = localization;
        if (ent == null) {
            ent = localizationsRepository.findById(localizationId).orElse(null);
        }

        if (ent == null) {
            throw new NotFoundException(String.format("Template localization #%s is not found", localizationId));
        }

        dtoToEntity(dto, ent);

        ent = localizationsRepository.saveAndFlush(ent);

        return ResponseEntity.ok(LocalizationDTO.of(ent));
    }

    @ApiOperation(value = "Create a template localization.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PostMapping("/api/templates/localizations")
    @Secured({Authority.PUSH_MARKETING})
    public ResponseEntity<LocalizationDTO> createTemplateLocalization(@RequestBody LocalizationDTO dto) {

        validateObject(dto);

        Localization localization = localizationsRepository
                .findByTemplateIdAndLocale(dto.getTemplateId(), dto.getLocaleIso()).orElse(null);

        if (localization != null) {
            throw new BadRequestException("Template locale with this localization exists.");
        }

        Localization ent = new Localization();

        dtoToEntity(dto, ent);

        ent = localizationsRepository.saveAndFlush(ent);

        return ResponseEntity.ok(LocalizationDTO.of(ent));
    }

    @ApiOperation(value = "Delete a template localization.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @DeleteMapping("/api/templates/localizations/{localizationId}")
    @Secured({Authority.PUSH_MARKETING})
    public ResponseEntity<AckDTO> deleteTemplateLocalization(@PathVariable Long localizationId) {

        if (!localizationsRepository.existsById(localizationId)) {
            throw new NotFoundException(String.format("Template localization \"%s\" is not exist", localizationId));
        }

        localizationsRepository.deleteById(localizationId);

        return ResponseEntity.ok(new AckDTO());
    }

    private void dtoToEntity(LocalizationDTO dto, Localization ent) {

        ent.setTitle(dto.getTitle());
        ent.setSubtitle(dto.getSubtitle());
        ent.setBody(dto.getBody());
        ent.setLocaleIso(dto.getLocaleIso());

        if (!dto.getTemplateId().equals(ent.getTemplateId())) {

            Optional<Template> template = templatesRepository.findById(dto.getTemplateId());

            if (!template.isPresent()) {
                throw new BadRequestException("Template with this id doesn't exist.");
            }

            ent.setTemplate(template.get());
        }
    }

}
