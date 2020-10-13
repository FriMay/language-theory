package language.theory.liksin.biriukov;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static language.theory.liksin.biriukov.ReservedWord.*;

public class Interpreter {

    private static final Integer MAX_VARIABLE_SIZE = 14;

    private static final Map<String, Integer> variables = new HashMap<>();

    public static void interpret(String enteredProgram) {

        if (!initVariables(enteredProgram)) {
            return;
        }
    }

    private static boolean initVariables(String enteredProgram) {

        String beforeVariables = VAR.getValue();
        String afterVariables =  COLON.getValue() + INTEGER.getValue() +  SEMICOLON.getValue();

        Matcher matcher = Pattern
                .compile(String.format("%s.+%s", beforeVariables, afterVariables))
                .matcher(enteredProgram);

        if (matcher.find()) {

            String initBlock = enteredProgram
                    .substring(matcher.start() + beforeVariables.length(), matcher.end() - afterVariables.length());

            String[] notInitVariables = initBlock.split(COMMA.getValue());

            for (String notInitVariable : notInitVariables) {

                if (notInitVariable.length() > MAX_VARIABLE_SIZE) {
                    System.out.printf(
                            "Variable \"%s\" length should be less equal than %s.\n",
                            notInitVariable,
                            MAX_VARIABLE_SIZE
                    );
                    return false;
                }

                ReservedWord reservedWord = ReservedWord.isReserved(notInitVariable);

                if (reservedWord != null) {
                    System.out.printf(
                            "Variable \"%s\" can't contain reserved word \"%s\".\n",
                            notInitVariable,
                            reservedWord.getValue()
                    );
                    return false;
                }

                Matcher badCharacterMatcher = Pattern.compile("[^a-z0-9]+").matcher(notInitVariable);

                if (badCharacterMatcher.find()) {
                    System.out.printf(
                            "Variable name should only consist number [0-9] and lowercase letters of the Latin alphabet [a-z].\nBad characters in \"%s\" record.\n",
                            notInitVariable
                    );
                    return false;
                }

                for (String declaredVariable : variables.keySet()) {
                    if (declaredVariable.equals(notInitVariable)) {
                        System.out.printf("Variable with \"%s\" name already declared.\n", notInitVariable);
                        return false;
                    }
                }

                variables.put(notInitVariable, null);
            }


        } else {
            System.out.println("Initialize block doesn't reached in current program.");
            return false;
        }

        return true;
    }
}
