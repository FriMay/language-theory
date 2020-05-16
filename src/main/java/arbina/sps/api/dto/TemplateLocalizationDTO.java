package arbina.sps.api.dto;

import arbina.infra.utils.ValidateField;
import arbina.sps.store.entity.TemplateLocalization;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Describes template localization.")
public class TemplateLocalizationDTO {

    @JsonProperty("id")
    @ApiParam(required = true)
    private Long id;

    @JsonProperty("title")
    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template title can't be empty.")
    private String title;

    @JsonProperty("subtitle")
    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template subtitle can't be empty.")
    private String subtitle;

    @JsonProperty("body")
    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template body can't be empty.")
    private String body;

    @JsonProperty("locale_iso")
    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template locale can't be empty.")
    private String localeIso;

    @JsonProperty("template_id")
    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template id can't be empty.")
    private Long templateId;

    public static TemplateLocalizationDTO of(TemplateLocalization ent) {

        return TemplateLocalizationDTO.builder()
                .id(ent.getId())
                .title(ent.getTitle())
                .subtitle(ent.getSubtitle())
                .body(ent.getBody())
                .localeIso(ent.getLocaleIso())
                .templateId(ent.getTemplateId())
                .build();
    }

}
