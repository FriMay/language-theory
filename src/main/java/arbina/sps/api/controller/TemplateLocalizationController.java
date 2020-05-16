package arbina.sps.api.controller;

import arbina.infra.dto.AckDTO;
import arbina.infra.dto.CursoredListBodyDTO;
import arbina.infra.dto.CursoredListDTO;
import arbina.infra.exceptions.BadRequestException;
import arbina.infra.exceptions.NotFoundException;
import arbina.infra.services.id.Authority;
import arbina.infra.utils.DtoUtils;
import arbina.sps.api.dto.TemplateLocalizationDTO;
import arbina.sps.config.SwaggerConfig;
import arbina.sps.store.entity.Template;
import arbina.sps.store.entity.TemplateLocalization;
import arbina.sps.store.repository.TemplateLocalizationRepository;
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
public class TemplateLocalizationController implements DtoUtils {

    private final TemplateLocalizationRepository templateLocalizationRepository;

    private final TemplatesRepository templatesRepository;

    public TemplateLocalizationController(TemplateLocalizationRepository templateLocalizationRepository,
                                          TemplatesRepository templatesRepository) {
        this.templateLocalizationRepository = templateLocalizationRepository;
        this.templatesRepository = templatesRepository;
    }

    @ApiOperation(value = "Fetch localization list for specified template.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @GetMapping("/api/templates/locales/{templateId}")
    @Secured({
            Authority.OBSERVER,
            Authority.EMAIL_MARKETING,
            Authority.EMAIL_NOTIFIER
    })
    public ResponseEntity<CursoredListBodyDTO<TemplateLocalizationDTO>> fetchTemplateLocalizations(
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "100") Integer limit,
            @PathVariable Long templateId) {

        if (templateId == null) {
            throw new BadRequestException("Template id can't be empty");
        }

        Stream<TemplateLocalization> templateLocalizationStream =
                templateLocalizationRepository.fetchAllByTemplateId(templateId);

        CursoredListDTO<TemplateLocalization, TemplateLocalizationDTO> dto =
                new CursoredListDTO<>(templateLocalizationStream.iterator(),
                        cursor, limit, TemplateLocalizationDTO::of);

        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Update a template localization.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PutMapping("/api/templates/locales/{templateLocalizationId}")
    @Secured({Authority.EMAIL_MARKETING})
    public ResponseEntity<TemplateLocalizationDTO> updateTemplateLocalization(
            @PathVariable Long templateLocalizationId,
            @RequestBody TemplateLocalizationDTO dto) {

        if (templateLocalizationId == null) {
            throw new BadRequestException("Template localization id can't be empty");
        }

        validateObject(dto);

        TemplateLocalization templateLocalization =
                templateLocalizationRepository
                        .findByTemplateIdAndLocale(dto.getTemplateId(), dto.getLocaleIso())
                        .orElse(null);

        if (templateLocalization != null && !templateLocalizationId.equals(templateLocalization.getId())) {
            throw new BadRequestException("Template localization with this locale exists, choice another locale.");
        }

        TemplateLocalization ent = templateLocalization;

        if (ent == null){
            ent = templateLocalizationRepository.findById(templateLocalizationId).orElse(null);
        }

        if (ent == null) {
            throw new NotFoundException(String.format("Template localization #%s is not found",
                    templateLocalizationId));
        }

        dtoToEntity(dto, ent);

        ent = templateLocalizationRepository.saveAndFlush(ent);

        return ResponseEntity.ok(TemplateLocalizationDTO.of(ent));
    }

    @ApiOperation(value = "Create a template localization.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PostMapping("/api/templates/locales")
    @Secured({Authority.EMAIL_MARKETING})
    public ResponseEntity<TemplateLocalizationDTO> createTemplateLocalization(
            @RequestBody TemplateLocalizationDTO dto) {

        validateObject(dto);

        TemplateLocalization templateLocalization =
                templateLocalizationRepository
                        .findByTemplateIdAndLocale(dto.getTemplateId(), dto.getLocaleIso())
                        .orElse(null);

        if (templateLocalization != null) {
            throw new BadRequestException("Template locale with this localization exists.");
        }

        TemplateLocalization ent = new TemplateLocalization();

        dtoToEntity(dto, ent);

        ent = templateLocalizationRepository.saveAndFlush(ent);

        return ResponseEntity.ok(TemplateLocalizationDTO.of(ent));
    }

    @ApiOperation(value = "Delete a template localization.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @DeleteMapping("/api/templates/locales/{templateLocalizationId}")
    @Secured({Authority.EMAIL_MARKETING})
    public ResponseEntity<AckDTO> deleteTemplateLocalization(@PathVariable Long templateLocalizationId) {

        if (!templateLocalizationRepository.existsById(templateLocalizationId)) {
            throw new NotFoundException(String.format("Template localization \"%s\" is not exist",
                    templateLocalizationId));
        }

        templateLocalizationRepository.deleteById(templateLocalizationId);

        return ResponseEntity.ok(new AckDTO());
    }

    private void dtoToEntity(TemplateLocalizationDTO dto, TemplateLocalization ent) {

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
