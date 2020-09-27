package language.theory.liksin.biriukov;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReplaceFunctionGenerator {

    public static List<ReplaceFunction> generateReplaceFunctions(Grammar grammar) {

        List<ReplaceFunction> replaceFunctions = new ArrayList<>();

        replaceFunctions.addAll(firstGenerationRule(grammar));

        replaceFunctions.addAll(secondGenerationRule(grammar));

        replaceFunctions.addAll(thirdGenerationRule(grammar));

        replaceFunctions.addAll(fourGenerationRule(grammar));

        return replaceFunctions;
    }

   private static List<ReplaceFunction> firstGenerationRule(Grammar grammar) {

        return grammar
                .getRules()
                .stream()
                .filter(it -> it.getValue().size() != 0 && grammar.getTerminalDictionary().contains(it.getValue().get(0)))
                .map(it -> {

                    List<String> ruleParts = it.getValue();

                    String terminal = ruleParts.get(0);

                    List<String> reverseArray = new ArrayList<>();

                    for (int i = ruleParts.size() - 1; i > 0; --i) {
                        reverseArray.add(ruleParts.get(i));
                    }

                    return ReplaceFunction.builder()
                            .leftReadOrder(terminal)
                            .rightReadOrder(it.getKey())
                            .writeOrder(reverseArray)
                            .isRead(true)
                            .build();
                })
                .collect(Collectors.toList());
   }

   private static List<ReplaceFunction> secondGenerationRule(Grammar grammar) {

        List<ReplaceFunction> replaceFunctions = new ArrayList<>();

        grammar.getRules()
                .stream()
                .filter(it -> it.getValue().size() != 0 && grammar.getNonTerminalDictionary().contains(it.getValue().get(0)))
                .forEach(rule -> {

                    String rightReadOrder = rule.getKey();

                    List<String> writeOrder = Lists.reverse(rule.getValue());

                    replaceFunctions.addAll(
                            grammar.getFirsts()
                                    .get(rule.getKey())
                                    .stream()
                                    .map(leftReadOrder ->
                                                ReplaceFunction.builder()
                                                        .leftReadOrder(leftReadOrder)
                                                        .rightReadOrder(rightReadOrder)
                                                        .writeOrder(writeOrder)
                                                        .isRead(false)
                                                        .build()
                                    ).collect(Collectors.toList())
                    );
                });

        return replaceFunctions;
   }

   private static List<ReplaceFunction> thirdGenerationRule(Grammar grammar) {

        List<ReplaceFunction> replaceFunctions = new ArrayList<>();

        grammar.getRules()
                .stream()
                .filter(it -> it.getValue().size() == 0)
                .forEach(rule -> {

                    String rightReadOrder = rule.getKey();

                    List<String> writeOrder = new ArrayList<>();

                    replaceFunctions.addAll(
                            grammar.getNexts()
                                    .get(rule.getKey())
                                    .stream()
                                    .map(leftReadOrder ->
                                                ReplaceFunction.builder()
                                                        .leftReadOrder(leftReadOrder)
                                                        .rightReadOrder(rightReadOrder)
                                                        .writeOrder(writeOrder)
                                                        .isRead(false)
                                                        .build()
                                    ).collect(Collectors.toList())
                    );
                });

        return replaceFunctions;
   }

   private static List<ReplaceFunction> fourGenerationRule(Grammar grammar) {

        Set<String> allFirsts = new HashSet<>();

        grammar.getFirsts()
                .values()
                .forEach(allFirsts::addAll);

        return grammar.getTerminalDictionary()
                .stream()
                .filter(it -> !allFirsts.contains(it))
                .map(it -> ReplaceFunction.builder()
                        .leftReadOrder(it)
                        .rightReadOrder(it)
                        .writeOrder(new ArrayList<>())
                        .isRead(true)
                        .build()
                ).collect(Collectors.toList());
   }
}
