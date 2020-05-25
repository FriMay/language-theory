package arbina.sps.api.dto;

import arbina.infra.utils.ValidateField;
import arbina.sps.store.entity.Localization;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Describes template localization.")
public class LocalizationDTO {

    @ApiParam(required = true)
    private Long id;

    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template title can't be empty.")
    private String title;

    @ApiParam(required = true)
    private String subtitle;

    @ApiParam(required = true)
    private String body;

    @JsonProperty("locale_iso")
    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template locale can't be empty.")
    private String localeIso;

    @JsonProperty("template_id")
    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template id can't be empty.")
    private Long templateId;

    public static LocalizationDTO of(Localization ent) {

        return LocalizationDTO.builder()
                .id(ent.getId())
                .title(ent.getTitle())
                .subtitle(ent.getSubtitle())
                .body(ent.getBody())
                .localeIso(ent.getLocaleIso())
                .templateId(ent.getTemplateId())
                .build();
    }

    public static List<LocalizationDTO> of(Stream<Localization> localizations) {

        return localizations.map(LocalizationDTO::of).collect(Collectors.toList());
    }
}
