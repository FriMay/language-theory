package language.theory.liksin.biriukov;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReplaceFunction {

    private String leftReadOrder;

    private String rightReadOrder;

    private List<String> writeOrder;

    private Boolean isRead;
}
