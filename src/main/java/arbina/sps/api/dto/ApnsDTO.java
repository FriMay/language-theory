package arbina.sps.api.dto;

import arbina.infra.utils.ValidateField;
import arbina.sps.store.entity.Apns;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Describes APNS(Apple Push Notification service).")
public class ApnsDTO {

    @JsonProperty("team_id")
    @ApiParam(required = true)
    @ValidateField(message = "Team id can't be empty!")
    private String teamId;

    @JsonProperty("key_id")
    @ApiParam(required = true)
    @ValidateField(message = "Key id can't be empty!")
    private String keyId;

    @JsonProperty("is_dev_gate")
    @ApiParam(required = true)
    @ValidateField(message = "Dev gate can't be empty!")
    private Boolean isDevGate;

    @JsonProperty("updated_at")
    private Date updatedAt;

    public static ApnsDTO of(Apns ent){

        return ApnsDTO.builder()
                .teamId(ent.getTeamId())
                .keyId(ent.getKeyId())
                .isDevGate(ent.getIsDevGate())
                .updatedAt(ent.getUpdatedAt())
                .build();
    }
}
