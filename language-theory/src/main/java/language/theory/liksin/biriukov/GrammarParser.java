package language.theory.liksin.biriukov;

import javafx.util.Pair;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class GrammarParser {

    ////////////////////////////Constants for regexp search//////////////////////////
    private static final String PARSER_SYMBOL = "`";

    private static final String TERMINAL_DICTIONARY_NAME = "Vt";
    private static final String NON_TERMINAL_DICTIONARY_NAME = "Va";
    private static final String RULES_NAME = "R";

    private static final String LEFT_SIDE_REGEXP = "=\\{";
    private static final String CENTER_SIDE_REGEXP = ".+?";
    private static final String RIGHT_SIDE_REGEXP = "\\}";

    private static final String EMPTY_STRING = "";

    private static final String TERMINAL_DIVIDER = "-";

    private static final String RULE_DIVIDER = "->";

    private static final String SUB_RULE_DIVIDER = "|";
    /////////////////////////////////////////////////////////////////////////////////

    private Set<String> terminalDictionary;

    private Set<String> nonTerminalDictionary;

    private List<Pair<String, String>> rules;

    public GrammarParser(String grammarString) {

        String grammarStringWithoutEscapes = grammarString.replaceAll("\\s+", EMPTY_STRING);

        this.rules = generateRules(grammarStringWithoutEscapes);

        this.terminalDictionary = generateSetByGrammarAndName(
                grammarStringWithoutEscapes,
                TERMINAL_DICTIONARY_NAME
        );

        this.nonTerminalDictionary = generateSetByGrammarAndName(
                grammarStringWithoutEscapes,
                NON_TERMINAL_DICTIONARY_NAME
        );
    }

    private List<Pair<String, String>> generateRules(String grammarString) {

        List<Pair<String, String>> rules = new ArrayList<>();

        findByRegexp(grammarString, RULES_NAME)
                .forEach(rule -> {

                    String[] ruleParts = rule.split(RULE_DIVIDER);

                    String ruleName = ruleParts[0];

                    String ruleDeclaration = "";

                    if (ruleParts.length != 1){
                        ruleDeclaration = ruleParts[1];
                    }

                    if (ruleDeclaration.contains(SUB_RULE_DIVIDER)) {
                        for (String subRule : ruleDeclaration.split("\\"+SUB_RULE_DIVIDER)) {
                            rules.add(new Pair<>(ruleName, subRule));
                        }
                    } else {
                        rules.add(new Pair<>(ruleName, ruleDeclaration));
                    }
                });

        return rules;
    }

    private Set<String> generateSetByGrammarAndName(String grammarString, String name) {

        Set<String> set = new HashSet<>();

        findByRegexp(grammarString, name)
                .forEach(terminal -> {

                    if (terminal.contains(TERMINAL_DIVIDER) && terminal.length() == 3) {

                        String[] dividedTerminal = terminal.split(TERMINAL_DIVIDER);

                        char startChar = dividedTerminal[0].charAt(0);
                        char endChar = dividedTerminal[1].charAt(0);

                        for (int i = startChar; i <= endChar; ++i) {
                            set.add(String.valueOf((char) i));
                        }
                    } else {
                        set.add(terminal);
                    }
                });

        return set
                .stream()
                .sorted((a, b) -> {

                    if (a.length() < b.length()) {
                        return -1;
                    }

                    if (a.length() > b.length()) {
                        return 1;
                    }

                    return a.compareTo(b);
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> findByRegexp(
            String searchString,
            String grammarPartName) {

        String regexp = String
                .format(
                        "%s%s%s%s",
                        grammarPartName,
                        LEFT_SIDE_REGEXP,
                        CENTER_SIDE_REGEXP,
                        RIGHT_SIDE_REGEXP
                );

        Matcher matcher = Pattern.compile(regexp).matcher(searchString);

        if (!matcher.find()) {
            throw new IllegalStateException(
                    String.format(
                            "Regexp \"%s\" not found in \"%s\"",
                            regexp,
                            searchString
                    )
            );
        }

        return Arrays.asList(
                searchString
                        .substring(matcher.start(), matcher.end())
                        .replaceFirst(grammarPartName + LEFT_SIDE_REGEXP, EMPTY_STRING)
                        .replaceFirst(
                                "(?s)" + RIGHT_SIDE_REGEXP + "(?!.*?" + RIGHT_SIDE_REGEXP + ")",
                                EMPTY_STRING
                        )
                        .split(PARSER_SYMBOL)
        );
    }
}
