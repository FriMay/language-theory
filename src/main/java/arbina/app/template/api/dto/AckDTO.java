package arbina.app.template.api.dto;

public class AckDTO {

    public Boolean ack;

    public static AckDTO of(Boolean ack) {

        AckDTO dto = new AckDTO();
        dto.ack = ack;

        return dto;
    }
}
