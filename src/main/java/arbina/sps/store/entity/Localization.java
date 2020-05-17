package arbina.sps.store.entity;

import arbina.infra.dto.CursoredListDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "localization")
public class Localization implements CursoredListDTO.Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "body")
    private String body;

    @Column(name = "local_iso")
    private String localeIso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", referencedColumnName = "id")
    private Template template;

    @Column(name = "template_id", insertable = false, updatable = false)
    private Long templateId;

    @Override
    public String getCursor() {
        return id.toString();
    }
}
