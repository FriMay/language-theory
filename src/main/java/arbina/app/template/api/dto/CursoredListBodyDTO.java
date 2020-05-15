package arbina.app.template.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CursoredListBodyDTO<T> {

    @JsonProperty("items")
    public List<T> items = new ArrayList<>();

    @JsonProperty("next_cursor")
    public String nextCursor;

    @JsonProperty("count")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long count;

    public CursoredListBodyDTO() {}
}