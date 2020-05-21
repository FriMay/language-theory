package arbina.sps.api.dto;

import arbina.sps.store.entity.Client;
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
@ApiModel(description = "Describes client.")
public class ClientDTO {

    @JsonProperty("client_id")
    private String clientId;

    private ApnsDTO apns;

    private FcmDTO fcm;

    @JsonProperty("is_configurable")
    private Boolean isConfigurable;

    public static ClientDTO of(Client ent){

        return ClientDTO.builder()
                .clientId(ent.getClientId())
                .apns(ApnsDTO.of(ent.getApns()))
                .fcm(FcmDTO.of(ent.getFcm()))
                .isConfigurable(ent.getIsConfigurable())
                .build();
    }

}
