package arbina.sps.api.dto;

import arbina.sps.store.entity.DeviceToken;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Describes device token.")
public class TokenDTO {

    @JsonProperty("id")
    public String id;

    public static TokenDTO of(DeviceToken ent) {

        return TokenDTO
                .builder()
                .id(ent.getId())
                .build();
    }
}
