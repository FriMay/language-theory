package language.theory.liksin.biriukov;

import java.util.Arrays;

public enum ReservedWord {

    VAR("var"),
    INTEGER("integer"),
    BEGIN("begin"),
    END("end"),
    WRITE("write"),
    READ("read"),
    FOR("for"),
    END_FOR("end_for"),
    TO("to"),
    DO("do"),
    COMMA(","),
    COLON(":"),
    SEMICOLON(";"),
    OPENING_BRACKET("("),
    CLOSING_BRACKET(")"),
    EQUALLY("="),
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*");

    private final String value;

    ReservedWord(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ReservedWord isReserved(String word) {
        return Arrays.stream(values())
                .filter(it -> word.toLowerCase().contains(it.getValue().toLowerCase()))
                .findFirst()
                .orElse(null);
    }
}
