package language.theory.liksin.biriukov;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static language.theory.liksin.biriukov.ReservedWord.*;

public class Interpreter {

    private static final Integer MAX_VARIABLE_SIZE = 14;

    private static final Map<String, Integer> variables = new HashMap<>();

    public static void computeEnteredProgram(String enteredProgram) {

        initVariables(enteredProgram);

        interpretProgram(enteredProgram);
    }

    private static void interpretProgram(String enteredProgram) {

        String program = getByRegexp(
                BEGIN.getValue(),
                END.getValue(),
                enteredProgram
        );

        if (program != null) {
            startInterpret(program);
        } else {
            throw new IllegalStateException("Program init block doesn't found.");
        }
    }

    private static void startInterpret(String currentProgram) {

        String currentLine = currentProgram.substring(
                0,
                currentProgram.indexOf(SEMICOLON.getValue())
        );

        if (currentProgram.startsWith(WRITE.getValue())) {

            currentProgram = currentProgram.substring(currentLine.length());

            String variableName = getByRegexp(
                    WRITE.getValue() + OPENING_BRACKET.getValue(),
                    CLOSING_BRACKET.getValue() + SEMICOLON.getValue(),
                    currentLine
            );

            System.out.println(getVariable(variableName));
        } else if (currentProgram.startsWith(READ.getValue())) {

            currentProgram = currentProgram.substring(currentLine.length());

            String variableName = getByRegexp(
                    READ.getValue() + OPENING_BRACKET.getValue(),
                    CLOSING_BRACKET.getValue() + SEMICOLON.getValue(),
                    currentLine
            );

            getVariable(variableName);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            int valueFromConsole;
            try {
                valueFromConsole = Integer.parseInt(reader.readLine());
            } catch (IOException | NumberFormatException e) {
                throw new IllegalStateException(e);
            }

            variables.put(variableName, valueFromConsole);
        } else if (currentProgram.startsWith(FOR.getValue())) {

            currentProgram = currentProgram.substring(FOR.getValue().length() + 1);

            String initLine = getByRegexp(OPENING_BRACKET.getValue(), CLOSING_BRACKET.getValue(), currentProgram);

            if (initLine == null) {
                throw new IllegalStateException("Init variable in cycle block doesn't found.");
            }

            initVariable(initLine);

            currentProgram = currentProgram.substring(currentProgram.indexOf(CLOSING_BRACKET.getValue()) + 1);
        }

        if (currentProgram.length() != 0) {
            startInterpret(currentProgram);
        }
    }

    private static void initVariable(String initLine) {

        String[] initArray = initLine.split("=");

        if (initArray.length != 2) {
            throw new IllegalStateException("Init block should contain one equal symbol!");
        }

        String variableName = initArray[0];

        getVariable(variableName);

        variables.put(variableName, getResultExpression(initArray[1]));
    }

    private static Integer getResultExpression(String computedExpression) {

        AtomicReference<String> expression = new AtomicReference<>(computedExpression);

        Arrays.stream(expression.get().split("^\\w+"))
                .forEach(it -> {

                    String currentExpressions = expression.get();

                    if (currentExpressions.contains(it)) {
                        expression.set(currentExpressions.replaceAll(it, getVariable(it).toString()));
                    }
                });

        String[] input = expression.get().split("");

        return ExpressionParser.compute(input);
    }

    private static Integer getVariable(String variableName) {

        if (!variables.containsKey(variableName)) {
            throw new IllegalStateException(String.format("Unknown variable name expected \"%s\".", variableName));
        }

        return variables.get(variableName);
    }

    private static void initVariables(String enteredProgram) {

        String initBlock = getByRegexp(
                VAR.getValue(),
                COLON.getValue() + INTEGER.getValue() + SEMICOLON.getValue(),
                enteredProgram
        );

        if (initBlock != null) {

            String[] notInitVariables = initBlock.split(COMMA.getValue());

            for (String notInitVariable : notInitVariables) {

                if (notInitVariable.length() > MAX_VARIABLE_SIZE) {
                    throw new IllegalStateException(
                            String.format(
                                    "Variable \"%s\" length should be less equal than %s.\n",
                                    notInitVariable,
                                    MAX_VARIABLE_SIZE
                            )
                    );
                }

                ReservedWord reservedWord = ReservedWord.isReserved(notInitVariable);

                if (reservedWord != null) {
                    throw new IllegalStateException(
                            String.format(
                                    "Variable \"%s\" can't contain reserved word \"%s\".\n",
                                    notInitVariable,
                                    reservedWord.getValue()
                            )
                    );
                }

                Matcher badCharacterMatcher = Pattern.compile("[^a-z0-9]+").matcher(notInitVariable);

                if (badCharacterMatcher.find()) {
                    throw new IllegalStateException(
                            String.format(
                                    "Variable name should only consist number [0-9] and" +
                                            " lowercase letters of the Latin alphabet [a-z]." +
                                            "\nBad characters in \"%s\" record.\n",
                                    notInitVariable
                            )
                    );
                }

                for (String declaredVariable : variables.keySet()) {
                    if (declaredVariable.equals(notInitVariable)) {
                        throw new IllegalStateException(
                                String.format(
                                        "Variable with \"%s\" name already declared.\n",
                                        notInitVariable
                                )
                        );
                    }
                }

                variables.put(notInitVariable, 0);
            }

        } else {
            throw new IllegalStateException("Initialize block doesn't reached in current program.");
        }
    }

    private static String getByRegexp(String before, String after, String text) {

        Matcher matcher = Pattern
                .compile(String.format("%s.+%s", before, after))
                .matcher(text);

        if (matcher.find()) {
            return text.substring(matcher.start() + before.length(), matcher.end() - after.length());
        } else {
            return null;
        }
    }
}
