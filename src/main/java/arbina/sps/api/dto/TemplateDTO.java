package arbina.sps.api.dto;

import arbina.infra.utils.ValidateField;
import arbina.sps.store.entity.Template;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Describes template.")
public class TemplateDTO {

    @ApiParam(required = true)
    private Long id;

    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template name can't be empty.")
    private String name;

    @ApiParam(required = true)
    @ValidateField(required = true, message = "Template description can't be empty.")
    private String description;

    @ApiParam(required = true)
    private Integer badge;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;

    @JsonProperty("last_used_at")
    private Date lastUsedAt;

    private Map<String, String> params;

    public static TemplateDTO of(Template ent) {

        return TemplateDTO.builder()
                .id(ent.getId())
                .name(ent.getName())
                .description(ent.getDescription())
                .createdAt(ent.getCreatedAt())
                .updatedAt(ent.getUpdatedAt())
                .lastUsedAt(ent.getLastUsedAt())
                .badge(ent.getBadge())
                .params(ent.getParams())
                .build();
    }
}
