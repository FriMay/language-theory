package arbina.sps.store.entity;

import arbina.infra.dto.CursoredListDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "device_token", indexes = {
        @Index(
                name = "device_token_username_token",
                columnList = "username, token",
                unique = true
        ),
        @Index(name = "device_token_username", columnList = "username")
})
public class DeviceToken implements CursoredListDTO.Entity {

    @Id
    @Column(name = "token")
    private String token;

    @Column(name = "username")
    private String username;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

    @Column(name = "client_id", insertable = false, updatable = false)
    private String clientId;

    @Override
    public String getCursor() {
        return token;
    }
}

