package arbina.app.template.api.dto;

import java.util.Iterator;
import java.util.function.Function;

public class CursoredListDTO<T extends CursoredListDTO.Entity, R> extends CursoredListBodyDTO<R> {

    public CursoredListDTO(Iterator<T> iterator,
                           String cursor,
                           Integer limit,
                           Function<T, R> mapper) {

        boolean collecting = false;

        while (iterator.hasNext()) {

            T ent = iterator.next();

            if (cursor.length() == 0 || ent.getCursor().equalsIgnoreCase(cursor)) {
                collecting = true;
            }

            if (collecting) {

                if (items.size() >= limit) {
                    nextCursor = ent.getCursor();
                    break;
                } else {
                    items.add(mapper.apply(ent));
                }
            }
        }
    }

    public interface Entity {
        String getCursor();
    };
}