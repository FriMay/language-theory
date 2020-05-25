package arbina.sps.store.entity;

import arbina.sps.api.dto.ApnsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Apns {

    @Lob
    @Column(name = "apns_certificate")
    private String apnsCertificate;

    @Column(name = "team_id")
    private String teamId;

    @Column(name = "key_id")
    private String keyId;

    @Column(name = "is_dev_gate")
    private Boolean isDevGate;

    @Column(name = "apns_updated_at")
    private Date updatedAt;

    public static Apns of(ApnsDTO dto) {

        return Apns.builder()
                .teamId(dto.getTeamId())
                .keyId(dto.getKeyId())
                .isDevGate(dto.getIsDevGate())
                .build();
    }

}
