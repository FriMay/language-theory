package language.theory.interpreter;

import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static language.theory.interpreter.ReservedWord.*;

public class Interpreter {

    private final Map<String, Integer> variables = new HashMap<>();

    private final StringBuilder answer = new StringBuilder();

    private final AtomicReference<String> params = new AtomicReference<>();

    public String computeEnteredProgram(String enteredProgram, String enteredParams) {

        enteredParams = enteredParams.replaceAll("\\s+", " ");

        params.set(enteredParams);

        enteredProgram = initVariables(enteredProgram);

        interpretProgram(enteredProgram);

        return answer.toString();
    }

    private void interpretProgram(String enteredProgram) {

        int cntEndFor = StringUtils.countOccurrencesOf(enteredProgram, END_FOR.getValue());
        int cntFor = StringUtils.countOccurrencesOf(enteredProgram, FOR.getValue()) - cntEndFor;

        if (cntFor != cntEndFor) {
            throw new IllegalStateException("Count operators \"for\" doesn't equals with count operators \"end_for\"!");
        }

        if (!enteredProgram.startsWith(BEGIN.getValue())) {
            throw new IllegalStateException(
                    String.format(
                            "Can't find operator \"%s\" in init block.",
                            BEGIN.getValue()
                    )
            );
        }

        if (!enteredProgram.endsWith(END.getValue())) {
            throw new IllegalStateException(
                    String.format(
                            "Can't find operator \"%s\" in init block.",
                            END.getValue()
                    )
            );
        }

        startInterpret(getByRegexp(
                BEGIN.getValue(),
                END.getValue(),
                enteredProgram
        ));
    }

    private void startInterpret(String currentProgram) {

        if (currentProgram.trim().length() == 0) {
            return;
        }

        String currentLine = currentProgram.substring(
                0,
                currentProgram.indexOf(SEMICOLON.getValue())
        );

        if (currentProgram.startsWith(WRITE.getValue())) {

            currentProgram = currentProgram.substring(currentLine.length() + 1);

            String variableName = getByRegexp(
                    WRITE.getValue() + OPENING_BRACKET.getValue(),
                    CLOSING_BRACKET.getValue(),
                    currentLine
            );

            answer.append(variableName).append("=").append(getVariable(variableName)).append("\n");
        } else if (currentProgram.startsWith(READ.getValue())) {

            currentProgram = currentProgram.substring(currentLine.length() + 1);

            String variableName = getByRegexp(
                    READ.getValue() + OPENING_BRACKET.getValue(),
                    CLOSING_BRACKET.getValue(),
                    currentLine
            );

            getVariable(variableName);

            int divider = params.get().indexOf(" ");

            String currentParam;

            if (divider != -1) {
                currentParam = params.get().substring(0, divider);
                params.set(params.get().substring(divider + 1));
            } else {
                currentParam = params.get();
                params.set("");
            }

            if (currentParam.trim().length() == 0) {
                throw new IllegalStateException(
                        String.format("Can't read value for %s variable, cause line with params empty. Add parameter to input line.", variableName)
                );
            }

            int value;
            try {
                value = Integer.parseInt(currentParam);
            } catch (NumberFormatException e) {
                throw new IllegalStateException(String.format("Can't set value for %s variable cause \"%s\" doesn't a number.", variableName, currentParam));
            }

            variables.put(variableName, value);
        } else if (currentProgram.startsWith(FOR.getValue())) {

            currentProgram = currentProgram.substring(FOR.getValue().length());

            if (!currentProgram.startsWith(OPENING_BRACKET.getValue())) {
                throw new IllegalStateException("Can't find opening bracket in cycle declaration.");
            }

            int index = currentProgram.indexOf(CLOSING_BRACKET.getValue());

            if (index == -1) {
                throw new IllegalStateException("Can't find closing bracket in cycle declaration.");
            }

            String initLine = currentProgram.substring(1, index);

            String from = initVariable(initLine);

            currentProgram = currentProgram.substring(index + 1);

            if (!currentProgram.startsWith(TO.getValue())) {
                throw new IllegalStateException("Can't find operator \"to\" in cycle declaration.");
            }

            currentProgram = currentProgram.substring(TO.getValue().length());

            index = currentProgram.indexOf(DO.getValue());

            if (index == -1) {
                throw new IllegalStateException("Can't find operator \"do\" in cycle declaration.");
            }

            Integer to = Integer.parseInt(currentProgram.substring(0, index));

            currentProgram = currentProgram.substring(index + DO.getValue().length());

            index = currentProgram.indexOf(END_FOR.getValue());

            if (index == -1) {
                throw new IllegalStateException("Can't find operator \"end_for\" in cycle declaration.");
            }

            int indexFor = getFor(currentProgram, 0);

            if (indexFor != -1) {

                int cntFor = 2;

                while (cntFor != 0) {

                    int newIndex = getFor(currentProgram, indexFor + 1);

                    if (newIndex == -1) {
                        cntFor--;
                        index = currentProgram.indexOf(END_FOR.getValue(), indexFor);
                        indexFor = index + END_FOR.getValue().length();
                    } else {
                        cntFor++;
                        indexFor = newIndex + 1;
                    }
                }
            }

            String subProgram = currentProgram.substring(0, index);

            while (getVariable(from) <= to) {

                startInterpret(subProgram);

                variables.put(from, getVariable(from) + 1);
            }

            currentProgram = currentProgram.substring(index + END_FOR.getValue().length() + SEMICOLON.getValue().length());

        } else {

            initVariable(currentLine);

            currentProgram = currentProgram.substring(currentLine.length() + 1);
        }

        if (currentProgram.length() != 0) {
            startInterpret(currentProgram);
        }
    }

    private Integer getFor(String string, Integer findFromIndex) {

        int indexFor = string.indexOf(FOR.getValue(), findFromIndex);
        int indexEndFor = string.indexOf(END_FOR.getValue(), findFromIndex);

        if (indexEndFor + (END_FOR.getValue().length() - FOR.getValue().length()) == indexFor) {
            return -1;
        }

        return indexFor;
    }

    private String initVariable(String initLine) {

        String[] initArray = initLine.split("=");

        if (initArray.length != 2) {
            throw new IllegalStateException("Init block should contain one equal symbol!");
        }

        String variableName = initArray[0];

        getVariable(variableName);

        variables.put(variableName, getResultExpression(initArray[1]));

        return variableName;
    }

    private Integer getResultExpression(String computedExpression) {

        AtomicReference<String> expression = new AtomicReference<>(computedExpression);

        Arrays.stream(expression.get().split("[^a-z]+"))
                .filter(it -> it.trim().length() > 0)
                .forEach(it -> {

                    String currentExpressions = expression.get();

                    if (currentExpressions.contains(it)) {
                        expression.set(currentExpressions.replaceAll(it, getVariable(it).toString()));
                    }
                });

        List<String> inputLines = new ArrayList<>();

        StringBuilder number = new StringBuilder();

        for (String s : expression.get().split("")) {
            try {
                Integer.parseInt(s);
                number.append(s);
            } catch (Exception e) {
                if (number.length() > 0) {
                    inputLines.add(number.toString());
                    number = new StringBuilder();
                }
                inputLines.add(s);
            }
        }

        if (number.length() != 0) {
            inputLines.add(number.toString());
        }

        return ExpressionParser.compute(inputLines.toArray(new String[0]));
    }

    private Integer getVariable(String variableName) {

        if (!variables.containsKey(variableName)) {
            throw new IllegalStateException(String.format("Unknown variable name expected \"%s\".", variableName));
        }

        return variables.get(variableName);
    }

    private String initVariables(String enteredProgram) {

        String initBlock = getByRegexp(
                VAR.getValue(),
                COLON.getValue() + INTEGER.getValue() + SEMICOLON.getValue(),
                enteredProgram
        );

        if (initBlock != null) {

            String[] notInitVariables = initBlock.split(COMMA.getValue());

            for (String notInitVariable : notInitVariables) {

                Integer MAX_VARIABLE_SIZE = 14;
                if (notInitVariable.length() > MAX_VARIABLE_SIZE) {
                    throw new IllegalStateException(
                            String.format(
                                    "Variable \"%s\" length should be less equal than %s.\n",
                                    notInitVariable,
                                    MAX_VARIABLE_SIZE
                            )
                    );
                }

                ReservedWord reservedWord = isReserved(notInitVariable);

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

        return enteredProgram.substring(VAR.getValue().length() + initBlock.length() + COLON.getValue().length() + INTEGER.getValue().length() + SEMICOLON.getValue().length());
    }

    private String getByRegexp(String before, String after, String text) {

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
