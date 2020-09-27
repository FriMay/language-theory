package test;

import language.theory.liksin.biriukov.*;
import org.junit.Test;

import java.util.List;

public class GrammarParserTest {

    @Test
    public void shouldCreateGrammar() {

        String stringGrammar =
                String.format("%s%s%s",
                        "Vt = { x` +` (` ) } ",
                        "Va = { A-C } ",
                        "R = {A -> x | (/B/)` B->A/C` C->+/A/C ` C->} "
                );

        Grammar grammar = GrammarParser.parseGrammar(stringGrammar);

        List<ReplaceFunction> replaceFunctionList
                = ReplaceFunctionGenerator.generateReplaceFunctions(grammar);

        ReplaceFunctionApplier.apply("(x+x)","A", replaceFunctionList);

        System.out.println(grammar);
    }

    @Test
    public void shouldReturnException() {

        String stringGrammar =
                String.format("%s%s%s",
                        "Vt = { a-z` 0-9` , ` var` begin` end } ",
                        "Va = { A-C } ",
                        "R = {A -> a/C/x | aBy` B->cA` C->} "
                );

        Grammar grammar = GrammarParser.parseGrammar(stringGrammar);

        System.out.println(grammar);
    }
}
