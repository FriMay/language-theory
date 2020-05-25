package arbina.sps.store.entity;

import arbina.infra.dto.CursoredListDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "client")
public class Client implements CursoredListDTO.Entity {

    @Id
    @Column(name = "id")
    private String clientId;

    @Embedded
    private Fcm fcm;

    @Embedded
    private Apns apns;

    @Column(name = "topic")
    private String topic;

    @Builder.Default
    @Column(name = "is_configurable")
    private Boolean isConfigurable = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private List<DeviceToken> tokens;

    @Override
    public String getCursor() {
        return clientId;
    }
}
