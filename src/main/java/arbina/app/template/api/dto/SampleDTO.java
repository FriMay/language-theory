package arbina.app.template.api.dto;

import arbina.app.template.store.entity.Sample;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SampleDTO {

    @JsonProperty("id")
    public String id;

    public static SampleDTO of(Sample ent) {

        SampleDTO dto = new SampleDTO();
        dto.id = ent.id;

        return dto;
    }
}
