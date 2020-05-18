package arbina.sps.store.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Param implements Serializable {

    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;

    @Column(name = "template_id", insertable = false, updatable = false)
    private Long templateId;

}
