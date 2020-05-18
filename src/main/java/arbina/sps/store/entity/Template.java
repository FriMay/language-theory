package arbina.sps.store.entity;

import arbina.infra.dto.CursoredListDTO;
import arbina.sps.api.dto.TemplateDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "template")
public class Template implements CursoredListDTO.Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "badge")
    private Integer badge;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "last_used_at")
    private Date lastUsedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "template_id", referencedColumnName = "id")
    private List<Localization> localizations;

    @ElementCollection
    @CollectionTable(name = "tempate_params", joinColumns = @JoinColumn(name = "key"))
    @Column(name = "value")
    private Map<String, String> params;

    @Override
    public String getCursor() {
        return id.toString();
    }

    public static void fromDTO(TemplateDTO dto, Template ent) {
        ent.setName(dto.getName());
        ent.setDescription(dto.getDescription());
        ent.setBadge(ent.getBadge());
        ent.setParams(dto.getParams());
    }

}
