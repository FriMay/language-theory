package arbina.sps.api.controller;

import arbina.infra.dto.AckDTO;
import arbina.infra.dto.CursoredListBodyDTO;
import arbina.infra.dto.CursoredListDTO;
import arbina.infra.exceptions.BadRequestException;
import arbina.infra.exceptions.NotFoundException;
import arbina.infra.services.id.Authority;
import arbina.infra.utils.DtoUtils;
import arbina.sps.api.dto.TemplateDTO;
import arbina.sps.config.SwaggerConfig;
import arbina.sps.store.entity.Template;
import arbina.sps.store.repository.TemplatesRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

@Controller
@Transactional
public class TemplatesController implements DtoUtils {
    private final TemplatesRepository templatesRepository;

    public TemplatesController(TemplatesRepository templatesRepository) {
        this.templatesRepository = templatesRepository;
    }

    @ApiOperation(value = "Fetch template list.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @GetMapping("/api/templates")
    @Secured({
            Authority.OBSERVER,
            Authority.EMAIL_MARKETING,
            Authority.EMAIL_NOTIFIER
    })
    public ResponseEntity<CursoredListBodyDTO<TemplateDTO>> fetchTemplates(
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "100") Integer limit,
            @RequestParam(required = false) String filter) {

        filter = Optional.ofNullable(filter).orElse("");

        Stream<Template> templatesStream;

        if (filter.length() > 0) {
            templatesStream = templatesRepository.findLikeName(filter);
        } else {
            templatesStream = templatesRepository.fetchAllSortedStream();
        }

        CursoredListDTO<Template, TemplateDTO> dto = new CursoredListDTO<>(templatesStream.iterator(),
                cursor, limit, TemplateDTO::of);

        return ResponseEntity.ok(dto);
    }

    @ApiOperation(value = "Create a template.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PostMapping("/api/templates")
    @Secured({Authority.EMAIL_MARKETING})
    public ResponseEntity<TemplateDTO> createTemplate(
            @RequestBody TemplateDTO dto) {

        validateObject(dto);

        Template ent = new Template();

        dtoToEntity(dto, ent);

        ent.setCreatedAt(new Date());
        ent.setUpdatedAt(new Date());

        ent = templatesRepository.saveAndFlush(ent);

        return ResponseEntity.ok(TemplateDTO.of(ent));
    }

    @ApiOperation(value = "Update a template.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @PutMapping("/api/templates/{templateId}")
    @Secured({Authority.EMAIL_MARKETING})
    public ResponseEntity<TemplateDTO> updateTemplate(
            @PathVariable Long templateId,
            @RequestBody TemplateDTO dto) {

        if (templateId == null) {
            throw new BadRequestException("Template id can't be empty");
        }

        validateObject(dto);

        Template ent = templatesRepository.findById(templateId).orElse(null);
        if (ent == null) {
            throw new NotFoundException(String.format("Template #%s is not found", templateId));
        }

        dtoToEntity(dto, ent);

        ent.setUpdatedAt(new Date());

        ent = templatesRepository.saveAndFlush(ent);

        return ResponseEntity.ok(TemplateDTO.of(ent));
    }

    @ApiOperation(value = "Delete a template.",
            authorizations = {@Authorization(value = SwaggerConfig.oAuth2)})
    @DeleteMapping("/api/templates/{templateId}")
    @Secured({Authority.EMAIL_MARKETING})
    public ResponseEntity<AckDTO> deleteTemplate(@PathVariable Long templateId) {

        if (!templatesRepository.existsById(templateId)) {
            throw new NotFoundException(String.format("Template \"%s\" is not exist", templateId));
        }

        templatesRepository.deleteById(templateId);

        return ResponseEntity.ok(new AckDTO());
    }

    private void dtoToEntity(TemplateDTO dto, Template ent) {
        ent.setName(dto.getName());
        ent.setDescription(dto.getDescription());
    }
}
