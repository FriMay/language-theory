package language.theory.interpreter;

import javafx.util.Pair;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Grammar {

    private Set<String> terminalDictionary;

    private Set<String> nonTerminalDictionary;

    private List<Pair<String, List<String>>> rules;

    private Map<String, Set<String>> firsts = new HashMap<>();

    private Map<String, Set<String>> nexts = new HashMap<>();

    public Grammar(Set<String> terminalDictionary,
                   Set<String> nonTerminalDictionary,
                   List<Pair<String, List<String>>> rules) {

        this.terminalDictionary = terminalDictionary;

        this.nonTerminalDictionary = nonTerminalDictionary;

        this.rules = rules;

        generateFirsts();

        generateNexts();

        checkFirstsAndNexts();
    }

    private void checkFirstsAndNexts() {

        Set<String> nonTerminalWithEmptyRule = rules.stream()
                .filter(it -> it.getValue().size() == 0)
                .map(Pair::getKey)
                .collect(Collectors.toSet());

        nonTerminalWithEmptyRule.forEach(nonTerminal -> {

            Set<String> first = firsts.get(nonTerminal);

            Set<String> next = nexts.get(nonTerminal);

            first.forEach(currentFirst -> {
                if (next.contains(currentFirst)) {
                    throw new IllegalStateException(
                            String.format(
                                    "This grammar isn't LL1 cause non terminal %s has firsts %s and nexts %s, which cross",
                                    nonTerminal,
                                    first,
                                    next)
                    );
                }
            });
        });
    }

    private void generateFirsts() {
        nonTerminalDictionary.forEach(it -> {
            if (!firsts.containsKey(it)) {
                generateFirsts(it);
            }
        });
    }

    private void generateNexts() {
        nonTerminalDictionary.forEach(it -> {
            if (!nexts.containsKey(it)) {
                generateNexts(it);
            }
        });
    }

    private Set<String> generateFirsts(String ruleName) {

        if (firsts.get(ruleName) == null) {
            firsts.put(ruleName, new HashSet<>());
        } else {
            return firsts.get(ruleName);
        }

        rules.stream()
                .filter(it -> it.getKey().equals(ruleName))
                .forEach(it -> {

                    Set<String> currentFirst = firsts.get(ruleName);

                    if (it.getValue().size() != 0) {

                        String current = it.getValue().get(0);

                        if (terminalDictionary.contains(current)) {
                            add(currentFirst, ruleName, Collections.singleton(current));
                        } else {
                            add(currentFirst, ruleName, generateFirsts(current));
                        }

                        rules.stream()
                                .filter(rp -> rp.getKey().equals(current) && rp.getValue().size() == 0)
                                .forEach(rp -> add(currentFirst, ruleName, generateNexts(rp.getKey())));
                    }
                });

        return firsts.get(ruleName);
    }

    private static void add(Set<String> firstSet, String ruleName, Set<String> added) {

        for (String first : added) {
            if (firstSet.contains(first)) {
                throw new IllegalStateException(
                        String.format("Rule \"%s\" already contain \"%s\" terminal.", ruleName, first)
                );
            }
            firstSet.add(first);
        }
    }

    private Set<String> generateNexts(String ruleName) {

        if (ruleName.equals("<СписокПеременных'>")) {
            System.out.println();
        }

        if (nexts.get(ruleName) == null) {
            nexts.put(ruleName, new HashSet<>());
        } else {
            return nexts.get(ruleName);
        }

        nexts.computeIfAbsent(ruleName, key -> new HashSet<>());

        rules.stream()
                .filter(it -> it.getValue().contains(ruleName))
                .forEach(it -> {

                    Set<String> currentNext = nexts.get(ruleName);

                    List<String> rightRule = it.getValue();

                    int index = rightRule.indexOf(ruleName) + 1;

                    if (index >= rightRule.size()) {
                        currentNext.addAll(generateNexts(it.getKey()));
                    } else {

                        String nextElement = rightRule.get(index);

                        if (terminalDictionary.contains(nextElement)) {
                            currentNext.add(nextElement);
                        } else {
                            currentNext.addAll(generateFirsts(nextElement));
                        }
                    }
                });

        return nexts.get(ruleName);
    }

    public String toString() {
        return String
                .format(
                        "\nGrammar declaration.\nTerminal dictionary: %s,\nNon terminal dictionary: %s,\nRules: %s,\nFirsts: %s,\nNexts: %s",
                        terminalDictionary,
                        nonTerminalDictionary,
                        rules.stream()
                                .map(it -> String.format(
                                        "%s=%s",
                                        it.getKey(),
                                        String.join("", it.getValue())
                                )).collect(Collectors.joining(", ")),
                        firsts,
                        nexts
                );
    }
}
