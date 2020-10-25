package language.theory;

//import language.theory.liksin.biriukov.Compiler;
//import language.theory.liksin.biriukov.*;
//import org.junit.Test;
//
//import java.util.List;
//
//import static language.theory.liksin.biriukov.GrammarParser.PARSER_SYMBOL;
//import static language.theory.liksin.biriukov.ReservedWord.*;

public class GrammarParserTest {
//
//    @Test
//    public void shouldCreateGrammar() {
//
//        String stringGrammar =
//                String.format("%s%s%s",
//                        "Vt = { " +
//                                VAR.getValue() + PARSER_SYMBOL +
//                                INTEGER.getValue() + PARSER_SYMBOL +
//                                BEGIN.getValue() + PARSER_SYMBOL +
//                                END.getValue() + PARSER_SYMBOL +
//                                WRITE.getValue() + PARSER_SYMBOL +
//                                READ.getValue() + PARSER_SYMBOL +
//                                FOR.getValue() + PARSER_SYMBOL +
//                                TO.getValue() + PARSER_SYMBOL +
//                                DO.getValue() + PARSER_SYMBOL +
//                                END_FOR.getValue() + PARSER_SYMBOL +
//                                COMMA.getValue() + PARSER_SYMBOL +
//                                COLON.getValue() + PARSER_SYMBOL +
//                                SEMICOLON.getValue() + PARSER_SYMBOL +
//                                OPENING_BRACKET.getValue() + PARSER_SYMBOL +
//                                CLOSING_BRACKET.getValue() + PARSER_SYMBOL +
//                                EQUALLY.getValue() + PARSER_SYMBOL +
//                                PLUS.getValue() + PARSER_SYMBOL +
//                                MINUS.getValue() + PARSER_SYMBOL +
//                                MULTIPLY.getValue() + PARSER_SYMBOL +
//                                "0-9" + PARSER_SYMBOL +
//                                "a-z" + PARSER_SYMBOL +
//                                "} ",
//                        "Va = { " +
//                                "<Программа>" + PARSER_SYMBOL +
//                                "<ОписаниеВычислений>" + PARSER_SYMBOL +
//                                "<ОписаниеПеременных>" + PARSER_SYMBOL +
//                                "<СписокПеременных>" + PARSER_SYMBOL +
//                                "<СписокПеременных'>" + PARSER_SYMBOL +
//                                "<СписокПрисваиваний>" + PARSER_SYMBOL +
//                                "<СписокПрисваиваний'>" + PARSER_SYMBOL +
//                                "<Присваивание>" + PARSER_SYMBOL +
//                                "<Выражение>" + PARSER_SYMBOL +
//                                "<Присваивание>" + PARSER_SYMBOL +
//                                "<Подвыражение>" + PARSER_SYMBOL +
//                                "<Подвыражение'>" + PARSER_SYMBOL +
//                                "<Операнд>" + PARSER_SYMBOL +
//                                "<Идентификатор>" + PARSER_SYMBOL +
//                                "<Идентификатор'>" + PARSER_SYMBOL +
//                                "<Константа>" + PARSER_SYMBOL +
//                                "<Буква>" + PARSER_SYMBOL +
//                                "<УнарныйОператор>" + PARSER_SYMBOL +
//                                "<БинарныйОператор>" + PARSER_SYMBOL +
//                                "<СписокДействий>" + PARSER_SYMBOL +
//                                "<СписокДействий'>" + PARSER_SYMBOL +
//                                " } ",
//                        "R = {" +
//                                "<Программа> -> <ОписаниеПеременных> / <ОписаниеВычислений>" + PARSER_SYMBOL +
//                                "<ОписаниеВычислений> -> " + BEGIN.getValue() + " / <ОписаниеВычислений'>" + PARSER_SYMBOL +
//                                "<ОписаниеВычислений'> -> <СписокПрисваиваний> | <СписокДействий>" + PARSER_SYMBOL +
//                                "<ОписаниеПеременных> -> " + VAR.getValue() + " / <СписокПеременных> / " + INTEGER.getValue() + " /" + SEMICOLON.getValue() + PARSER_SYMBOL +
//                                "<СписокПеременных> -> <Идентификатор> / <СписокПеременных'>" + PARSER_SYMBOL +
//                                "<СписокПеременных'> -> " + COMMA.getValue() + " / <СписокПеременных> |" + COLON.getValue() + " | " + PARSER_SYMBOL +
//                                "<СписокПрисваиваний> -> <Присваивание> / <СписокПрисваиваний'> | " + END.getValue() + PARSER_SYMBOL +
//                                "<СписокПрисваиваний'> -> <СписокПрисваиваний> |" + PARSER_SYMBOL +
//                                "<Присваивание> -> <Идентификатор> / " + EQUALLY.getValue() + " / <Выражение> / " + SEMICOLON.getValue() + PARSER_SYMBOL +
//                                "<Выражение> -> <УнарныйОператор> / <Подвыражение> | <Подвыражение>" + PARSER_SYMBOL +
//                                "<Подвыражение> -> " + OPENING_BRACKET.getValue() + " / <Выражение> / " + CLOSING_BRACKET.getValue() + " / <Подвыражение'> " +
//                                "| <Операнд> / <Подвыражение'>" + PARSER_SYMBOL +
//                                "<Подвыражение'> -> <БинарныйОператор> / <Подвыражение> |" + PARSER_SYMBOL +
//                                "<Операнд> -> <Идентификатор> " +
//                                "| <Константа>" + PARSER_SYMBOL +
//                                "<Идентификатор> -> <Буква> / <Идентификатор'>" + PARSER_SYMBOL +
//                                "<Идентификатор'> -> <Идентификатор> |" + PARSER_SYMBOL +
//                                "<Константа> -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9" + PARSER_SYMBOL +
//                                "<Буква> -> a | b | c | d | e | f | g | h | i | j | k | l | m | n | o | p | q | r | s | t | u | v | w | x | y | z" + PARSER_SYMBOL +
//                                "<УнарныйОператор> -> " + MINUS.getValue() + PARSER_SYMBOL +
//                                "<БинарныйОператор> -> " + PLUS.getValue() + " | " + MINUS.getValue() + " | " + MULTIPLY.getValue() + PARSER_SYMBOL +
//                                "<СписокДействий> -> " + READ.getValue() + " / " + OPENING_BRACKET.getValue() + " / <Идентификатор> / " + CLOSING_BRACKET.getValue() + " / " + SEMICOLON.getValue() + " / <СписокДействий'> " +
//                                "| " + WRITE.getValue() + " / " + OPENING_BRACKET.getValue() + " / <Идентификатор> / " + CLOSING_BRACKET.getValue() + " / " + SEMICOLON.getValue() + " / <СписокДействий'> " +
//                                "| " + FOR.getValue() + " / " + OPENING_BRACKET.getValue() + " / <Присваивание> / " + CLOSING_BRACKET.getValue() + " / " + TO.getValue() + " / " + OPENING_BRACKET.getValue() + " / <Выражение> / " + CLOSING_BRACKET.getValue() + " / " + DO.getValue() + " / <СписокДействий'> / " + END_FOR.getValue() + " / " + SEMICOLON.getValue() + PARSER_SYMBOL +
//                                "<СписокДействий'> -> <СписокДействий> | <СписокПрисваиваний> |" +
//                                "}"
//                );
//
////        Grammar grammar = GrammarParser.parseGrammar(stringGrammar);
////
////        List<ReplaceFunction> replaceFunctionList
////                = ReplaceFunctionGenerator.generateReplaceFunctions(grammar);
//
//        String testProgram = ("var a,b,i: integer;" +
//                "begin" +
//                "b = 7;" +
//                "read(a);" +
//                "for (i=5) to 6 do" +
//                "write(a);" +
//                "write(b);" +
//                "end").replaceAll("\\s+", "");
//
//        Interpreter.computeEnteredProgram(testProgram);
//
////        if (Compiler.compile(testProgram, "<Программа>", replaceFunctionList)) {
////
////        }
//    }
}
