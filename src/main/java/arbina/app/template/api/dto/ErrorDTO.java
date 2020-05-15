package arbina.app.template.api.dto;

public class ErrorDTO {
    public String error;
    public String error_description;

    public static ErrorDTO of(String error,
                              String error_description) {

        ErrorDTO dto = new ErrorDTO();

        dto.error = error;
        dto.error_description = error_description;

        return dto;
    }
}
