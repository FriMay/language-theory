package arbina.sps.store.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Fcm {

    @Column(name = "fcm_config")
    private String config;

    @Column(name = "fcm_updated_at")
    private Date updatedAt;

}
