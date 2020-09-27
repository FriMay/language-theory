package test;

import language.theory.liksin.biriukov.GrammarParser;
import org.junit.Test;

public class GrammarParserTest {

    @Test
    public void shouldCreateGrammarParser() {

        String stringGrammar =
                String.format("%s%s%s",
                        "Vt = { a-z` 0-9` , ` var` begin` end } ",
                        "Va = { A-Z } ",
                        "R = {A -> aC | cA` B->cB` C->} "
                );

        GrammarParser grammarParser = new GrammarParser(stringGrammar);

        System.out.printf("Source grammatical: %s%n", stringGrammar);
        System.out.printf("Terminal dictionary: %s%n", grammarParser.getTerminalDictionary());
        System.out.printf("Not terminal dictionary: %s%n", grammarParser.getNonTerminalDictionary());
        System.out.printf("Rules list: %s%n", grammarParser.getRules());
    }
}
