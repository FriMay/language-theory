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
public class ClientConfigDTO {

    @JsonProperty("client_id")
    private String clientId;

    private ApnsDTO apns;

    private FcmDTO fcm;

    private String topic;

    @JsonProperty("is_configurable")
    private Boolean isConfigurable;

    public static ClientConfigDTO of(Client ent) {

        return ClientConfigDTO.builder()
                .clientId(ent.getClientId())
                .apns(ApnsDTO.of(ent.getApns()))
                .fcm(FcmDTO.of(ent.getFcm()))
                .topic(ent.getTopic())
                .isConfigurable(ent.getIsConfigurable())
                .build();
    }

}
