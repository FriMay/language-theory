package arbina.sps.api.dto;

import arbina.sps.store.entity.Fcm;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Describes FCM(Firebase Cloud Messaging).")
public class FcmDTO {

    @JsonProperty("updated_at")
    private Date updatedAt;

    public static FcmDTO of(Fcm ent) {

        if (ent == null)
            return null;

        return FcmDTO.builder()
                .updatedAt(ent.getUpdatedAt())
                .build();
    }
}
