package arbina.app.template.store.entity;

import arbina.app.template.api.dto.CursoredListDTO;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sample")
public class Sample implements CursoredListDTO.Entity {

    @Id
    public String id;

    public Sample() {}

    @Override
    public String getCursor() {
        return id;
    }
}
